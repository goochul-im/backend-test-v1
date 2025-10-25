package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentPage
import im.bigs.pg.application.payment.port.out.PaymentSummaryProjection
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.util.Base64
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QueryPaymentsServiceTest {

    private val paymentRepository = mockk<PaymentOutPort>()
    private lateinit var queryPaymentsService: QueryPaymentsService

    @BeforeEach
    fun setUp() {
        queryPaymentsService = QueryPaymentsService(paymentRepository)
    }

    @Test
    fun `cursor가 null일 때 첫번째 페이지를 반환해야 한다`() {
        // given
        val filter = QueryFilter(partnerId = 1, limit = 1)
        val paymentPage = PaymentPage(
            items = listOf(
                createPayment(1L, LocalDateTime.of(2025, 10, 25, 10, 0, 1))
            ),
            hasNext = true,
            nextCursorCreatedAt = LocalDateTime.of(2025, 10, 25, 10, 0, 1),
            nextCursorId = 1L
        )
        val summary = PaymentSummaryProjection(
            count = 100L,
            totalAmount = BigDecimal("1000000"),
            totalNetAmount = BigDecimal("950000")
        )

        every { paymentRepository.findBy(any()) } returns paymentPage
        every { paymentRepository.summary(any()) } returns summary

        // when
        val result = queryPaymentsService.query(filter)

        // then
        assertEquals(result.items.size, 1)
        assertEquals(result.summary.count, 100L)
        assertEquals(result.summary.totalAmount, BigDecimal("1000000"))
        assertEquals(result.summary.totalNetAmount, BigDecimal("950000"))
        assertTrue(result.hasNext)

        verify {
            paymentRepository.findBy(withArg {
                assertNull(it.cursorCreatedAt)
                assertNull(it.cursorCreatedAt)
            })
        }
    }

    @Test
    fun `cursor가 주어졌을 때 다음 페이지 조회를 조회해야 한다`() {
        // given
        val testLocalDatetime = LocalDateTime.of(2025, 10, 25, 10, 0, 1)
        val cursor = encodeCursor(testLocalDatetime.toInstant(java.time.ZoneOffset.UTC), 1L)
        val filter = QueryFilter(partnerId = 1, limit = 10, cursor = cursor)

        val paymentPage = PaymentPage(
            items = listOf(createPayment(2L, LocalDateTime.of(2025, 10, 25, 10, 0, 2))),
            hasNext = true,
            nextCursorCreatedAt = LocalDateTime.of(2025, 10, 25, 10, 0, 2),
            nextCursorId = 2L
        )
        val summary = PaymentSummaryProjection(
            count = 100L,
            totalAmount = BigDecimal("1000000"),
            totalNetAmount = BigDecimal("950000")
        )

        every { paymentRepository.findBy(any()) } returns paymentPage
        every { paymentRepository.summary(any()) } returns summary

        // when
        val result = queryPaymentsService.query(filter)

        // then
        assertEquals(result.items.size, 1)

        verify {
            paymentRepository.findBy(withArg {
                assertEquals(it.cursorId, 1L)
                assertEquals(it.cursorCreatedAt, testLocalDatetime)
            })
        }
    }

    @Test
    fun `마지막 페이지를 조회할 때 다음 커서는 없어야 한다`() {
        // given
        val cursor = "testcursor"
        val filter = QueryFilter(partnerId = 1, limit = 10, cursor = cursor)

        val paymentPage = PaymentPage(
            items = listOf(createPayment(2L, LocalDateTime.now())),
            hasNext = false,
            nextCursorCreatedAt = null,
            nextCursorId = null
        )
        val summary = PaymentSummaryProjection(
            count = 100L,
            totalAmount = BigDecimal("1000000"),
            totalNetAmount = BigDecimal("950000")
        )

        every { paymentRepository.findBy(any()) } returns paymentPage
        every { paymentRepository.summary(any()) } returns summary

        // when
        val result = queryPaymentsService.query(filter)

        // then
        assertEquals(result.items.size, 1)
        assertContains(result.items.map { it.id }, 2L)
        assertNull(result.nextCursor)
    }

    private fun createPayment(id: Long, createdAt: LocalDateTime): Payment {
        return Payment(
            id = id,
            partnerId = 1L,
            amount = BigDecimal.TEN,
            appliedFeeRate = BigDecimal.ZERO,
            feeAmount = BigDecimal.ZERO,
            netAmount = BigDecimal.TEN,
            cardBin = "123456",
            cardLast4 = "7890",
            approvalCode = "12345678",
            approvedAt = LocalDateTime.now(),
            status = PaymentStatus.APPROVED,
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }

    private fun encodeCursor(createdAt: Instant?, id: Long?): String? {
        if (createdAt == null || id == null) return null
        val raw = "${createdAt.toEpochMilli()}:$id"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray())
    }
}
