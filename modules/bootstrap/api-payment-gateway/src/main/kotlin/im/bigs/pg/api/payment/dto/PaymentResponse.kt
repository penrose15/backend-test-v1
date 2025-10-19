package im.bigs.pg.api.payment.dto

import com.fasterxml.jackson.annotation.JsonFormat
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentResponse(
    @field:Schema(description = "결제 식별자", example = "1")
    val id: Long?,

    @field:Schema(description = "제휴사 식별자", example = "1")
    val partnerId: Long,

    @field:Schema(description = "금액", example = "10000")
    val amount: BigDecimal,

    @field:Schema(description = "적용된 수수료 비율", example = "0.01")
    val appliedFeeRate: BigDecimal,

    @field:Schema(description = "수수료 금액", example = "100")
    val feeAmount: BigDecimal,

    @field:Schema(description = "순 금액", example = "9900")
    val netAmount: BigDecimal,

    @field:Schema(description = "카드 뒷자리 4자리", example = "1234")
    val cardLast4: String?,

    @field:Schema(description = "승인 코드", example = "00000000")
    val approvalCode: String,

    @field:Schema(description = "승인 시간", example = "2025-01-01 00:00:00")
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val approvedAt: LocalDateTime,

    @field:Schema(description = "결제 상태", example = "APPROVED")
    val status: PaymentStatus,

    @field:Schema(description = "생성 날짜", example = "2025-01-01 00:00:00")
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
