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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.util.Collections

@Component
class TestPgClient(
    private val aes256Encryptor: PgEncryptor,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${pgclient.test-pg.url}")
    private var API_URL: String,
    @Value("\${pgclient.test-pg.api-key}")
    private var API_KEY: String
) : PgClientOutPort {

    private val log = LoggerFactory.getLogger(javaClass)
    private val prefixMessage = "[TestPG Client]"

    override fun supports(partnerId: Long): Boolean {
        return partnerId == 2L
    }

    override fun approve(request: PgApproveRequest): PgApproveResult {

        val encValue = aes256Encryptor.encrypt(request.copy(amount = BigDecimal(request.amount.toInt())))
        val headers = HttpHeaders()
        headers.set("Content-Type", "application/json")
        headers.set("API-KEY", API_KEY)
        val requestBody = Collections.singletonMap("enc", encValue)
        val requestEntity = HttpEntity(requestBody, headers)

        try {

            val response: ResponseEntity<TestPgSuccessResponse> = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                requestEntity,
                TestPgSuccessResponse::class.java,
            )

            val responseBody = response.body ?: run {
                log.error("TestPg 2xx OK 응답을 받았으나, body가 null입니다.")
                throw PgClientException(addPrefixMessageToException("응답 데이터가 비어있습니다."))
            }

            return PgApproveResult(
                approvalCode = responseBody.approvalCode,
                approvedAt = responseBody.approvedAt,
                status = PaymentStatus.valueOf(responseBody.status),
            )
        } catch (e: HttpClientErrorException) {
            // e.statusCode (e.g., 422, 401)
            // e.responseBodyAsString (오류 JSON 문자열)
            log.error("$prefixMessage TestPg사와의 통신이 실패하였습니다!! (4xx) message = ${e.message}")
            val errorResponse = objectMapper.readValue(e.responseBodyAsString, TestPgFailureResponse::class.java)
            when (e.statusCode.value()) {
                422 -> throw PgClientException(addPrefixMessageToException(get422ExceptionMessage(errorResponse)))
                401 -> throw PgClientException(addPrefixMessageToException("요청 형식에 문제가 있습니다. message = ${e.message}"))
                else -> throw PgClientException(addPrefixMessageToException("알수 없는 에러. message = ${e.message}"))
            }

        } catch (e: HttpServerErrorException) {
            // [5xx 오류 처리]
            log.error("TestPg 서버에 문제가 있습니다!! (5xx) message = ${e.message}")
            throw PgClientException(addPrefixMessageToException("서버에 에러가 있습니다. message = ${e.message}"))
        } catch (e: Exception) {
            log.error("PG 승인 처리 중 알 수 없는 예외가 발생하였습니다.", e)
            throw PgClientException(addPrefixMessageToException("PG 승인 처리 중 내부 오류 발생: ${e.message}"))
        }
    }

    private fun get422ExceptionMessage(response: TestPgFailureResponse): String {
        var message = ""
        when (response.code) {
            1001 -> message = "도난 또는 분실된 카드입니다."
            1002 -> message = "한도가 초과되었습니다."
            1003 -> message = "정지되었거나 만료된 카드입니다."
            1004 -> message = "위조 또는 변조된 카드입니다."
            1005 -> message = "위조 또는 변조된 카드입니다.(허용되지 않은 카드)"
        }
        return message
    }

    private fun addPrefixMessageToException(message: String): String {
        return "$prefixMessage $message"
    }

}
