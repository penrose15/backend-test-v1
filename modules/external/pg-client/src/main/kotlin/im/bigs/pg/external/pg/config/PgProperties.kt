package im.bigs.pg.external.pg.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "pg.api")
class PgProperties(
    val url: String,
    val apiKey: String,
    val iv: String,
    val connectTimeout: Long,
    val readTimeout: Long,
)
