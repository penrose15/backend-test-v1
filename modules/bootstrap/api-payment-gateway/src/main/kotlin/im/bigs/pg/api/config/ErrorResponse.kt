package im.bigs.pg.api.config

import java.time.LocalDateTime

data class ErrorResponse(
    val status: Int,
    val message: String?,
    val exception: String? = null,
    val timestamp: LocalDateTime? = LocalDateTime.now(),
)
