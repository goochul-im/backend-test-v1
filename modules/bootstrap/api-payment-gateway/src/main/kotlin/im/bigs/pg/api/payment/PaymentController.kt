package im.bigs.pg.api.payment

import im.bigs.pg.application.payment.port.`in`.PaymentUseCase
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.`in`.*
import im.bigs.pg.api.payment.dto.CreatePaymentRequest
import im.bigs.pg.api.payment.dto.PaymentResponse
import im.bigs.pg.api.payment.dto.QueryResponse
import im.bigs.pg.api.payment.dto.Summary
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * 결제 API 진입점.
 * - POST: 결제 생성
 * - GET: 결제 조회(커서 페이지네이션 + 통계)
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "결제 API")
@Validated
class PaymentController(
    private val paymentUseCase: PaymentUseCase,
    private val queryPaymentsUseCase: QueryPaymentsUseCase,
) {

    /** 결제 생성 요청 페이로드(간소화된 필드). */
    

    /** API 응답을 위한 변환용 DTO. 도메인 모델을 그대로 노출하지 않습니다. */
    

    /**
     * 결제 생성.
     *
     * @param req 결제 요청 본문
     * @return 생성된 결제 요약 응답
     */
    @Operation(summary = "결제 요청" ,description = "제휴사에 결제 요청을 보냅니다")
    @ApiResponse(
        responseCode = "200",
        description = "결제 성공",
        content = [Content(schema = Schema(implementation = PaymentResponse::class))]
    )
    @PostMapping
    fun create(@RequestBody req: CreatePaymentRequest): ResponseEntity<PaymentResponse> {
        val saved = paymentUseCase.pay(
            PaymentCommand(
                partnerId = req.partnerId,
                amount = req.amount,
                cardBin = req.cardBin,
                cardLast4 = req.cardLast4,
                productName = req.productName,
            ),
        )
        return ResponseEntity.ok(PaymentResponse.from(saved))
    }

    /**
     * 결제 조회(커서 기반 페이지네이션 + 통계).
     *
     * @param partnerId 제휴사 필터
     * @param status 상태 필터
     * @param from 조회 시작 시각(ISO-8601)
     * @param to 조회 종료 시각(ISO-8601)
     * @param cursor 다음 페이지 커서
     * @param limit 페이지 크기(기본 20)
     * @return 목록/통계/커서 정보
     */
    @Operation(summary = "결제 조회", description = "커서와 필터 조건을 기반으로 유저의 상세 정보를 가져옵니다.")
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = [Content(schema = Schema(implementation = QueryResponse::class))]
    )
    @GetMapping
    fun query(
        @Parameter(description = "제휴사 id, 추가하지 않으면 모든 제휴사를 조회합니다.", example = "2", required = false)
        @RequestParam(required = false) partnerId: Long?,
        @Parameter(description = "결제 상태, 추가하지 않으면 모든 제휴사를 가져옵니다.", required = false, schema = Schema(
            type = "string",
            allowableValues = ["APPROVED", "CANCEL"]
        ))
        @RequestParam(required = false) status: String?,
        @Parameter(description = "시작 날짜", example = "2025-10-01 12:58:30", required = false)
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") from: LocalDateTime?,
        @Parameter(description = "끝 날짜", example = "2025-10-03 05:00:00", required = false)
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") to: LocalDateTime?,
        @Parameter(description = "이전 페이지의 nextCursor, 추가하지 않으면 첫 페이지를 가져옵니다.", example = "MxKsqosjqQks", required = false)
        @RequestParam(required = false) cursor: String?,
        @Parameter(description = "한 페이지 당 가져올 결제 내역의 수, 추가하지 않으면 20개를 가져옵니다", example = "20", required = false)
        @RequestParam(defaultValue = "20") limit: Int,
    ): ResponseEntity<QueryResponse> {
        val res = queryPaymentsUseCase.query(
            QueryFilter(partnerId, status, from, to, cursor, limit),
        )
        return ResponseEntity.ok(
            QueryResponse(
                items = res.items.map { PaymentResponse.from(it) },
                summary = Summary(res.summary.count, res.summary.totalAmount, res.summary.totalNetAmount),
                nextCursor = res.nextCursor,
                hasNext = res.hasNext,
            ),
        )
    }

}
