package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.Length
import java.math.BigDecimal

data class CreatePaymentRequest(
    @field:Schema(description = "제휴사 식별자", example = "1", required = true)
    val partnerId: Long,

    @field:Schema(description = "금액", example = "1000", required = true)
    @field:Min(1)
    val amount: BigDecimal,

    @field:Schema(description = "카드 bin(6자리)", example = "123456", required = true)
    @field:Length(min = 6, max = 6)
    val cardBin: String? = null,

    @field:Schema(description = "카드 마지막 4자리", example = "1234", required = true)
    @field:Length(min = 4, max = 4)
    val cardLast4: String? = null,

    @field:Schema(description = "생년월일", example = "19900101", required = true)
    @field:Length(min = 8, max = 8)
    val birthDate: String? = null,

    @field:Schema(description = "카드 만료날짜", example = "1227", required = true)
    @field:Length(min = 4, max = 4)
    val expiry: String? = null,

    @field:Schema(description = "카드 비밀번호 앞 2자리", example = "12", required = true)
    @field:Length(min = 2, max = 2)
    val password: String? = null,

    @field:Schema(description = "구매 물품", example = "물품", required = false)
    val productName: String? = null,
)
