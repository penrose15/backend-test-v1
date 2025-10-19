package im.bigs.pg.external.pg.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PgPlainRequest(
    @JsonProperty("cardNumber")
    val cardNumber: String? = null,
    @JsonProperty("birthDate")
    val birthDate: String? = null,
    @JsonProperty("expiry")
    val expiry: String? = null,
    @JsonProperty("password")
    val password: String? = null,
    @JsonProperty("amount")
    val amount: Int,
) {
    companion object {
        fun getTestPgPlainRequest(
            cardBin: String? = null,
            cardLast4: String? = null,
            birthDate: String? = null,
            expiry: String? = null,
            password: String? = null,
            amount: Int
        ): PgPlainRequest {
            val rawCardNumber = "$cardBin$cardBin$cardLast4"
            // 카드 번호가 cardBin(6자리) + 6자리 + cardLast4 형태가 되도록 함
            val cardNumber = rawCardNumber.chunked(4).joinToString("-")

            return PgPlainRequest(
                cardNumber = cardNumber,
                birthDate = birthDate,
                expiry = expiry,
                password = password,
                amount = amount,
            )
        }
    }
}
