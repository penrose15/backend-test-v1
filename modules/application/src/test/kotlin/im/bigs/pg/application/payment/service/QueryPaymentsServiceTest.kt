package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.out.*
import im.bigs.pg.application.payment.service.fixture.PaymentFixture
import im.bigs.pg.domain.payment.Payment
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class QueryPaymentsServiceTest {
    private val paymentRepo = object : PaymentOutPort
    {
        override fun save(payment: Payment) = PaymentFixture.make(1L)
        override fun findBy(query: PaymentQuery): PaymentPage = PaymentFixture.makes()
        override fun summary(filter: PaymentSummaryFilter): PaymentSummaryProjection = PaymentFixture.makeSummaryProjection()
    }

    @Test
    fun `페이지네이션과 통계 집계를 반환한다`() {
        val queryPaymentsService = QueryPaymentsService(paymentRepo)
        val queryResult = queryPaymentsService.query(QueryFilter(
            partnerId = 1L,
            status = "APPROVED",
            from = LocalDateTime.of(2025,1,1,0,0).minusDays(1L),
            to = LocalDateTime.of(2025,1,1,0,0)
        ))

        assertEquals(queryResult.hasNext, true)
        assertEquals(queryResult.items.size, 20)
        assertNotNull(queryResult.nextCursor)
        assertEquals(queryResult.summary.count, 25)
        assertEquals(queryResult.summary.totalAmount, BigDecimal(10000))
        assertEquals(queryResult.summary.totalNetAmount, BigDecimal(9000))
    }
}