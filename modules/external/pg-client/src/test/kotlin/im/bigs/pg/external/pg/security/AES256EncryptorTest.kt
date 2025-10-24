package im.bigs.pg.external.pg.security

import im.bigs.pg.application.pg.port.out.PgApproveRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Base64

class AES256EncryptorTest {

    private lateinit var encryptor: AES256Encryptor

    @BeforeEach
    fun setUp() {
        encryptor = AES256Encryptor()
    }

    @Test
    fun `Encryptor는 Base64로 정확히 암호화되어야한다`() {
        val request = PgApproveRequest(
            partnerId = 1L,
            amount = BigDecimal(10000),
            cardBin = "123456",
            cardLast4 = "1234",
            productName = "Test Product"
        )

        val encryptedText = encryptor.encrypt(request)

        assertNotNull(encryptedText)
        assertTrue(encryptedText.isNotEmpty())
        assertDoesNotThrow { Base64.getUrlDecoder().decode(encryptedText) }
    }

}
