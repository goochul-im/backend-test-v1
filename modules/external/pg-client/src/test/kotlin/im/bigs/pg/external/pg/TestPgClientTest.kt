package im.bigs.pg.external.pg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.external.pg.exception.PgClientException
import im.bigs.pg.external.pg.security.PgEncryptor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

import im.bigs.pg.external.pg.dto.TestPgFailureResponse
import im.bigs.pg.external.pg.dto.TestPgSuccessResponse
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime

class TestPgClientTest {

    private lateinit var testPgClient: TestPgClient

    private val pgEncryptor: PgEncryptor = mockk<PgEncryptor>()

    private val restTemplate: RestTemplate = mockk<RestTemplate>()

    private val objectMapper = jacksonObjectMapper()

    private val API_URL = "http://fake-pg-api.com"
    private val API_KEY = "fake-api-key"

    @BeforeEach
    fun setUp() {
        testPgClient = TestPgClient(pgEncryptor, restTemplate, objectMapper, API_URL, API_KEY)
    }

    private fun createPgApproveRequest(): PgApproveRequest {
        return PgApproveRequest(
            partnerId = 1L,
            amount = BigDecimal("10000"),
            productName = "Test Product",
            cardBin = "123456",
            cardLast4 = "1234"
        )
    }

    @Test
    fun `approve는 PG 성공 응답 시 성공 결과를 반환해야 한다`() {
        // Given
        val request = createPgApproveRequest()
        val encryptedValue = "encrypted_test_data"
        val successResponse = TestPgSuccessResponse(
            "ApprovedCode123",
            LocalDateTime.now(),
            "1234",
            amount = BigDecimal("10000"),
            status = "APPROVED"
        )

        every { pgEncryptor.encrypt(request) } returns encryptedValue

        every {
            restTemplate.exchange(
                eq(API_URL),
                eq(HttpMethod.POST),
                any<HttpEntity<*>>(),
                eq(TestPgSuccessResponse::class.java)
            )
        } returns ResponseEntity(successResponse, HttpStatus.OK)

        // When
        val result = testPgClient.approve(request)

        // Then
        assertNotNull(result)
        assertEquals("ApprovedCode123", result.approvalCode)
        assertEquals(PaymentStatus.APPROVED, result.status)

        verify { pgEncryptor.encrypt(request) }
        verify {
            restTemplate.exchange(
                eq(API_URL),
                eq(HttpMethod.POST),
                any<HttpEntity<*>>(),
                eq(TestPgSuccessResponse::class.java)
            )
        }
    }

    @Test
    fun `approve는 PG로부터 4xx 오류 수신 시 PgClientException을 던져야 한다`() {
        // Given
        val request = createPgApproveRequest()
        val encryptedValue = "encrypted_test_data"
        val errorResponseBody = objectMapper.writeValueAsString(TestPgFailureResponse(
            errorCode = "STOLEN_OR_LOST",
            code = 1001,
            message = "도난 또는 분실된 카드입니다.",
            referenceId = "ref_id"
        ))

        every { pgEncryptor.encrypt(request) } returns encryptedValue
        
        every {
            restTemplate.exchange(
                eq(API_URL),
                eq(HttpMethod.POST),
                any<HttpEntity<*>>(),
                eq(TestPgSuccessResponse::class.java)
            )
        } throws HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "422 Unprocessable Entity",
            null,
            errorResponseBody.toByteArray(),
            Charsets.UTF_8)

        // When/Then
        assertThrows<PgClientException> {
            testPgClient.approve(request)
        }

        verify { pgEncryptor.encrypt(request) }
        verify {
            restTemplate.exchange(
                eq(API_URL),
                eq(HttpMethod.POST),
                any<HttpEntity<*>>(),
                eq(TestPgSuccessResponse::class.java)
            )
        }
    }

    @Test
    fun `approve는 PG로부터 5xx 오류 수신 시 PgClientException을 던져야 한다`() {
        // Given
        val request = createPgApproveRequest()
        val encryptedValue = "encrypted_test_data"

        every { pgEncryptor.encrypt(request) } returns encryptedValue
        
        every {
            restTemplate.exchange(
                eq(API_URL),
                eq(HttpMethod.POST),
                any<HttpEntity<*>>(),
                eq(TestPgSuccessResponse::class.java)
            )
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        // When/Then
        assertThrows<PgClientException> {
            testPgClient.approve(request)
        }

        verify { pgEncryptor.encrypt(request) }
        verify {
            restTemplate.exchange(
                eq(API_URL),
                eq(HttpMethod.POST),
                any<HttpEntity<*>>(),
                eq(TestPgSuccessResponse::class.java)
            )
        }
    }

    @Test
    fun `approve는 PG 승인 처리 중 알 수 없는 예외 발생 시 PgClientException을 던져야 한다`() {
        // Given
        val request = createPgApproveRequest()
        val encryptedValue = "encrypted_test_data"

        every { pgEncryptor.encrypt(request) } returns encryptedValue
        
        every {
            restTemplate.exchange(
                eq(API_URL),
                eq(HttpMethod.POST),
                any<HttpEntity<*>>(),
                eq(TestPgSuccessResponse::class.java)
            )
        } throws RuntimeException("Network is down")

        // When/Then
        assertThrows<PgClientException> {
            testPgClient.approve(request)
        }

        verify { pgEncryptor.encrypt(request) }
        verify {
            restTemplate.exchange(
                eq(API_URL),
                eq(HttpMethod.POST),
                any<HttpEntity<*>>(),
                eq(TestPgSuccessResponse::class.java)
            )
        }
    }
}
