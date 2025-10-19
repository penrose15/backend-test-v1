package im.bigs.pg.external.pg.dto

data class PgResponse(
    val approvalCode: String,
    val approvedAt: String,
    val maskedCardLast4: String,
    val amount: Int,
    val status: String,
)
