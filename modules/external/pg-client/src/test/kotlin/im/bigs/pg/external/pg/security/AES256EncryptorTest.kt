package im.bigs.pg.external.pg.security

import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.external.pg.exception.EncryptFailedException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Base64

class AES256EncryptorTest {

    private lateinit var encryptor: AES256Encryptor

    @BeforeEach
    fun setUp() {
        encryptor = AES256Encryptor(
            TEST_IV_BASE64URL = "TESTURL",
            ALGORITHM = "AES/GCM/NoPadding",
            API_KEY = "TESTAPIKEY"
        )
    }

    @Test
    fun `Encryptor는 Base64로 정확히 암호화되어야한다`() {
        val request = makeRequest()

        val encryptedText = encryptor.encrypt(request)

        assertNotNull(encryptedText)
        assertTrue(encryptedText.isNotEmpty())
        assertDoesNotThrow { Base64.getUrlDecoder().decode(encryptedText) }
    }

    @Test
    fun `환경변수가 잘못 입력되면 예외가 발생한다`() {
        val request = makeRequest()
        encryptor = AES256Encryptor(
            "TESTURL",
            "BAD_ALGORITHM",
            "TESTALGORITHM"
        )

        assertThrows(EncryptFailedException::class.java) { encryptor.encrypt(request) }
    }

    private fun makeRequest(): PgApproveRequest = PgApproveRequest(
        partnerId = 1L,
        amount = BigDecimal(10000),
        cardBin = "123456",
        cardLast4 = "1234",
        productName = "Test Product"
    )
}
