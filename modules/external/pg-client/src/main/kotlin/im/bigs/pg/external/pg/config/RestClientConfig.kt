package im.bigs.pg.external.pg.config

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.external.pg.exception.PayFailedException
import im.bigs.pg.external.pg.exception.dto.PgErrorResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class RestClientConfig(
    private val pgProperties: PgProperties,
    private val objectMapper: ObjectMapper
) {
    companion object {
        const val API_KEY_HEADER = "API-KEY"
    }

    @Bean
    fun restClient(): RestClient {
        val requestFactory = SimpleClientHttpRequestFactory()
        requestFactory.setConnectTimeout(Duration.ofSeconds(pgProperties.connectTimeout))
        requestFactory.setReadTimeout(Duration.ofSeconds(pgProperties.readTimeout))

        return RestClient.builder()
            .baseUrl(pgProperties.url)
            .defaultHeader(API_KEY_HEADER, pgProperties.apiKey)
            .requestFactory(requestFactory) // timeout 설정
            .defaultStatusHandler({ code -> !code.is2xxSuccessful }) { _, response ->
                when (val status = response.statusCode.value()) {
                    422 -> {
                        val responseBody = response.body.readAllBytes()
                            .toString(Charsets.UTF_8)
                        val result = objectMapper.readValue(responseBody, PgErrorResponse::class.java)
                        throw PayFailedException("PG 요청 실패: ${result.errorCode}, ${result.message}")
                    }

                    401 -> {
                        throw PayFailedException("PG 인증 실패: $status")
                    }

                    else -> {
                        throw PayFailedException("PG 요청 실패: $status")
                    }
                }
            }
            .build()
    }
}
