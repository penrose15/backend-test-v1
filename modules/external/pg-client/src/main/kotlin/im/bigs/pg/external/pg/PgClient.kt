package im.bigs.pg.external.pg

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.external.pg.config.PgProperties
import im.bigs.pg.external.pg.dto.PgPlainRequest
import im.bigs.pg.external.pg.dto.PgRequest
import im.bigs.pg.external.pg.dto.PgResponse
import im.bigs.pg.external.pg.exception.PayFailedException
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class PgClient(
    private val pgProperties: PgProperties,
    private val objectMapper: ObjectMapper,
    private val restClient: RestClient
) : PgClientOutPort {
    companion object {
        const val APPROVE_URL = "/api/v1/pay/credit-card"
        const val KEY_ALGORITHM = "SHA-256"
        const val SECRET_KEY_ALGORITHM = "AES"
        const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH = 128
    }

    override fun supports(partnerId: Long): Boolean = partnerId > 1L

    override fun approve(request: PgApproveRequest): PgApproveResult {
        // 요청 body 암호화
        val enc = encodeBody(request)

        val response = restClient.post()
            .uri {
                it.path(APPROVE_URL)
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(PgRequest(enc))
            .retrieve()
            .body(PgResponse::class.java) ?: throw PayFailedException("PG 응답이 없습니다.")

        val approvedAt = response.approvedAt.let {
            LocalDateTime.parse(it)
        }

        return PgApproveResult(
            approvalCode = response.approvalCode,
            approvedAt = approvedAt
        )
    }

    fun encodeBody(request: PgApproveRequest): String {
        val base64Encoder = Base64.getUrlEncoder().withoutPadding()

        // key 암호화
        val md = MessageDigest.getInstance(KEY_ALGORITHM)
        md.update(pgProperties.apiKey.toByteArray(StandardCharsets.UTF_8))
        val keyBytes = md.digest()

        // iv decoded
        val ivDecoded = Base64.getUrlDecoder().decode(pgProperties.iv)
        require(ivDecoded.size == 12) { "IV must be 12 bytes" }

        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        val keySpec = SecretKeySpec(keyBytes, SECRET_KEY_ALGORITHM)
        val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, ivDecoded)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec)

        // plain request 생성
        val plainRequest = objectMapper.writeValueAsString(
            PgPlainRequest.getTestPgPlainRequest(
                request.cardBin,
                request.cardLast4,
                request.birthDate,
                request.expiry,
                request.password,
                request.amount.toInt()
            )
        ).trimIndent()

        // cipher text 생성
        val cipherText = cipher.doFinal(plainRequest.toByteArray(StandardCharsets.UTF_8))
        return base64Encoder.encodeToString(cipherText)
    }
}
