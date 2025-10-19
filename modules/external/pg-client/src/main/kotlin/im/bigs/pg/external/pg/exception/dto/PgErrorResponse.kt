package im.bigs.pg.external.pg.exception.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PgErrorResponse(
    @JsonProperty("code")
    val code: Int,
    @JsonProperty("errorCode")
    val errorCode: String,
    @JsonProperty("message")
    val message: String,
    @JsonProperty("referenceId")
    val referenceId: String?,
)
