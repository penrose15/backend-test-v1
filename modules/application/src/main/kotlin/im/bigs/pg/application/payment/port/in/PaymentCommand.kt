package im.bigs.pg.application.payment.port.`in`

import java.math.BigDecimal

/**
 * 결제 생성에 필요한 최소 입력.
 *
 * @property partnerId 제휴사 식별자
 * @property amount 결제 금액(정수 금액 권장)
 * @property cardBin 카드 BIN(없을 수 있음)
 * @property cardLast4 카드 마지막 4자리(없을 수 있음)
 * @property birthDate 생년월일(없을 수 있음)
 * @property expiry 카드 유효기간(없을 수 있음)
 * @property password 카드 비밀번호(없을 수 있음)
 * @property productName 상품명(없을 수 있음)
 */
data class PaymentCommand(
    val partnerId: Long,
    val amount: BigDecimal,
    val cardBin: String? = null,
    val cardLast4: String? = null,
    val birthDate: String? = null,
    val expiry: String? = null,
    val password: String? = null,
    val productName: String? = null,
)
