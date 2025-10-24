package im.bigs.pg.external.pg.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime

data class TestPgSuccessResponse(
    val approvalCode: String,
    @get:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    val approvedAt: LocalDateTime,
    val maskedCardLast4: String,
    val amount: BigDecimal,
    val status: String
)
