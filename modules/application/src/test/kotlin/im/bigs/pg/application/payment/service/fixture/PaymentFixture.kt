package im.bigs.pg.application.payment.service.fixture

import im.bigs.pg.application.payment.port.out.PaymentPage
import im.bigs.pg.application.payment.port.out.PaymentSummaryProjection
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentFixture {
    companion object {
        fun make(id: Long): Payment {
            return Payment(
                id = id,
                partnerId = 1L,
                amount = BigDecimal("1000"),
                appliedFeeRate = BigDecimal("0.1"),
                feeAmount = BigDecimal("100"),
                netAmount = BigDecimal("900"),
                cardBin = "123456",
                cardLast4 = "1234",
                approvalCode = "123121",
                approvedAt = LocalDateTime.of(2025,1,1,0,0).plusSeconds(id),
                status = PaymentStatus.APPROVED,
                createdAt = LocalDateTime.of(2025,1,1,0,0).plusSeconds(id),
                updatedAt = LocalDateTime.of(2025,1,1,0,0).plusSeconds(id),
            )
        }

        fun makes(): PaymentPage {
            val items = (5..< 25).map { make((30 - it).toLong()) }

            val nextCursorAt = LocalDateTime.of(2025,1,1,0,0).plusSeconds(6L)
            return PaymentPage(items, true, nextCursorAt, 6L)
        }

        fun makeSummaryProjection(): PaymentSummaryProjection {
            return PaymentSummaryProjection(25L, BigDecimal(10000), BigDecimal(9000))
        }
    }
}