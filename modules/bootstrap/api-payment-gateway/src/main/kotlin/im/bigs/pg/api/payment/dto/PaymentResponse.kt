package im.bigs.pg.api.payment.dto

import com.fasterxml.jackson.annotation.JsonFormat
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

@Schema(description = "결제 세부 내역")
data class PaymentResponse(
    @get:Schema(description = "결제 id", example = "1")
    val id: Long?,
    @get:Schema(description = "제휴사 id", example = "1")
    val partnerId: Long,
    @get:Schema(description = "결제 총 금액", example = "60000")
    val amount: BigDecimal,
    @get:Schema(description = "수수료율", example = "0.0235")
    val appliedFeeRate: BigDecimal,
    @get:Schema(description = "수수료", example = "1000")
    val feeAmount: BigDecimal,
    @get:Schema(description = "정산 금액", example = "59000")
    val netAmount: BigDecimal,
    @get:Schema(description = "카드 마지막 4자리", example = "1234")
    val cardLast4: String?,
    @get:Schema(description = "결제 코드", example = "10257740")
    val approvalCode: String,
    @get:Schema(description = "결제 승인 시간", example = "2025-10-01 12:58:30")
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val approvedAt: LocalDateTime,
    @get:Schema(description = "결제 상태", example = "APPROVED")
    val status: PaymentStatus,
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @get:Schema(description = "결제 생성 시간", example = "2025-10-01 12:58:30")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(p: Payment) = PaymentResponse(
            id = p.id,
            partnerId = p.partnerId,
            amount = p.amount,
            appliedFeeRate = p.appliedFeeRate,
            feeAmount = p.feeAmount,
            netAmount = p.netAmount,
            cardLast4 = p.cardLast4,
            approvalCode = p.approvalCode,
            approvedAt = p.approvedAt,
            status = p.status,
            createdAt = p.createdAt,
        )
    }
}

