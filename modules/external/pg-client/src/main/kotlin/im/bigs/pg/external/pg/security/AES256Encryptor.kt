package im.bigs.pg.external.pg.security

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import im.bigs.pg.external.pg.exception.EncryptFailedException
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class AES256Encryptor(
    @param:Value("\${pgclient.test-pg.iv-base64url}")
    private var TEST_IV_BASE64URL: String,
    @param:Value("\${pgclient.test-pg.algorithm}")
    private var ALGORITHM: String,
    @param:Value("\${pgclient.test-pg.api-key}")
    private var API_KEY: String
) : PgEncryptor {

    private val TAG_LENGTH_BIT: Int = 128
    private val mapper = jacksonObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    data class TestPgRequest(
        val cardNumber: String,
        val birthDate: String,
        val expiry: String,
        val password: String,
        val amount: String
    )

    override fun encrypt(request: PgApproveRequest): String {
        try {
            val secretKey = apiKeyToSecretKey(API_KEY)

            val iv = decodeBase64Url(TEST_IV_BASE64URL) // IllegalArgumentException 가능

            val plaintTextBytes = mapper.writeValueAsBytes(TestPgRequest(
                cardNumber = "1111-1111-1111-1111",
                birthDate = "19900101",
                expiry = "1227",
                password = "12",
                amount = request.amount.toString()
            )) // JsonProcessingException

            val gcmParameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)

            val cipher = Cipher.getInstance(ALGORITHM) // NoSuchAlgorithmException
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec) // InvalidKeyException

            val encryptedBytes = cipher.doFinal(plaintTextBytes) // IllegalBlockSizeException

            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes)

        } catch (e: JsonProcessingException) {
            // 암호화 불가능한 데이터
            throw EncryptFailedException("암호화 불가능한 데이터입니다. message = ${e.message}")
        } catch (e: GeneralSecurityException) {
            // Cipher.getInstance, init, doFinal
            throw EncryptFailedException("환경변수가 올바르지 않습니다. message = ${e.message}")
        } catch (e: IllegalArgumentException) {
            // Base64 디코딩 실패
            throw EncryptFailedException("Base64 디코딩이 실패하였습니다. message = ${e.message}")
        } catch (e: Exception) {
            // 기타
            throw EncryptFailedException("암호화가 실패하였습니다. message = ${e.message}")
        }
    }

    private fun apiKeyToSecretKey(value: String): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(value.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(
            keyBytes,
            "AES"
        )
    }

    private fun decodeBase64Url(value: String): ByteArray {
        return Base64.getUrlDecoder().decode(value)
    }

}
