package im.bigs.pg.external.pg

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.external.pg.dto.TestPgFailureResponse
import im.bigs.pg.external.pg.dto.TestPgSuccessResponse
import im.bigs.pg.external.pg.exception.PgClientException
import im.bigs.pg.external.pg.security.PgEncryptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.Collections

@Component
class TestPgClient(
    private val aes256Encryptor: PgEncryptor,
    private val restTemplate: RestTemplate,
) : PgClientOutPort {

    @Value("\${pgclient.test-pg.url}")
    private lateinit var API_URL: String

    @Value("\${pgclient.test-pg.api-key}")
    private lateinit var API_KEY: String

    private val objectMapper = ObjectMapper()

    override fun supports(partnerId: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun approve(request: PgApproveRequest): PgApproveResult {

        val encValue = aes256Encryptor.encrypt(request)
        val headers = HttpHeaders()
        headers.set("Content-Type", "application/json")
        headers.set("API-KEY", API_KEY)
        val requestEntity = HttpEntity<Unit>(headers)

        try {

            val response: ResponseEntity<TestPgSuccessResponse> = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                requestEntity,
                TestPgSuccessResponse::class.java,
                Collections.singletonMap("enc", encValue)
            )

            return PgApproveResult( // 응답이 못 들어올 때는 어떤 데이터를 주어야 하는가??
                approvalCode = response.body?.approvalCode ?: "THIS-PAY-IS-CANCELED",
                approvedAt = response.body?.approvedAt ?: LocalDateTime.now(),
                status = PaymentStatus.valueOf(response.body?.status ?: "CANCELED"),
            )
        } catch (e: HttpClientErrorException) {
            // e.statusCode (e.g., 422, 401)
            // e.responseBodyAsString (오류 JSON 문자열)
            val errorResponse = objectMapper.readValue(e.responseBodyAsString, TestPgFailureResponse::class.java)
            when (e.statusCode.value()) {
                422 -> TODO()
                401 -> TODO()
            }

        } catch (e: HttpServerErrorException) {
            // [5xx 오류 처리]
            throw PgClientException("TestPG 서버에 에러가 있습니다. message = ${e.message}")
        }

        return TODO("반환 값을 제공하세요")
    }

    private fun throw422Exception(response: TestPgFailureResponse) {
        when (response.errorCode.toInt()) {
            1001 -> TODO()
            1002 -> TODO()
            1003 -> TODO()
            1004 -> TODO()
            1005 -> TODO()
        }
    }

}
