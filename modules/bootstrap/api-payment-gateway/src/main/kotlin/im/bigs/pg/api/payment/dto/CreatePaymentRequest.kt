package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import java.math.BigDecimal

@Schema(description = "결제 요청 dto")
data class CreatePaymentRequest(
    @get:Schema(description = "제휴사 식별 id", required = true, example = "2")
    val partnerId: Long,
    @field:Min(1)
    @get:Schema(description = "결제 요청 금액, 최소 1이상 있어야 합니다.", required = true, example = "10000")
    val amount: BigDecimal,
    @get:Schema(description = "카드 BIN", example = "123456", required = false)
    val cardBin: String? = null,
    @get:Schema(description = "카드 마지막 4자리", example = "1234", required = false)
    val cardLast4: String? = null,
    @get:Schema(description = "제품 이름", example = "Test Product", required = false)
    val productName: String? = null,
)

