package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "결제 내역 조회 응답")
data class QueryResponse(
    @get:Schema(description = "결제 내역 아이템 리스트")
    val items: List<PaymentResponse>,
    @get:Schema(description = "필터 결제 내역 요약 정보")
    val summary: Summary,
    @get:Schema(description = "페이지 결제 내역 요약 정보")
    val pageSummary: Summary,
    @get:Schema(description = "인코딩된 다음 커서", example = "eyJwYXJ0bmVySWQiOjEsImxhc3RJZCI6MTIzNDV9")
    val nextCursor: String?,
    @get:Schema(description = "다음 페이지가 존재하는지의 여부")
    val hasNext: Boolean,
)

@Schema(description = "결제 내역 요약 정보")
data class Summary(
    @get:Schema(description = "총 결제 건수")
    val count: Long,
    @get:Schema(description = "총 결제 금액 (원)")
    val totalAmount: BigDecimal,
    @get:Schema(description = "수수료를 제외한 총 정산 금액")
    val totalNetAmount: BigDecimal,
)

