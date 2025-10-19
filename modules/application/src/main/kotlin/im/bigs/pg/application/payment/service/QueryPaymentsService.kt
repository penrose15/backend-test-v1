package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.`in`.QueryPaymentsUseCase
import im.bigs.pg.application.payment.port.`in`.QueryResult
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.application.payment.port.out.PaymentSummaryFilter
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.domain.payment.PaymentSummary
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.util.*

/**
 * 결제 이력 조회 유스케이스 구현체.
 * - 커서 토큰은 createdAt/id를 안전하게 인코딩해 전달/복원합니다.
 * - 통계는 조회 조건과 동일한 집합을 대상으로 계산됩니다.
 */
@Service
class QueryPaymentsService(
    private val paymentRepository: PaymentOutPort
) : QueryPaymentsUseCase {
    /**
     * 필터를 기반으로 결제 내역을 조회합니다.
     *
     * 현재 구현은 과제용 목업으로, 빈 결과를 반환합니다.
     * 지원자는 커서 기반 페이지네이션과 통계 집계를 완성하세요.
     *
     * @param filter 파트너/상태/기간/커서/페이지 크기
     * @return 조회 결과(목록/통계/커서)
     */
    override fun query(filter: QueryFilter): QueryResult {
        /*
        * from ~ to 까지 partenerId 기준으로 item 조회, 커서 기반으로 페이징 처리 및 통계 계산
        * */
        val zoneId = ZoneId.of("Asia/Seoul")

        val decodedCursor = decodeCursor(filter.cursor)
        val cursorCreatedAt = decodedCursor.first?.atZone(zoneId)?.toLocalDateTime()
        val cursorId = decodedCursor.second

        val items = paymentRepository.findBy(
            PaymentQuery(
                partnerId = filter.partnerId,
                status = filter.status?.let {
                    PaymentStatus.valueOf(it)
                },
                from = filter.from,
                to = filter.to,
                limit = filter.limit,
                cursorCreatedAt = cursorCreatedAt,
                cursorId = cursorId
            )
        )

        val summaryProjection = paymentRepository.summary(
            PaymentSummaryFilter(
                partnerId = filter.partnerId,
                status = filter.status?.let {
                    PaymentStatus.valueOf(it)
                },
                from = filter.from,
                to = filter.to
            )
        )

        val nextCursorCreatedAt = items.nextCursorCreatedAt?.atZone(zoneId)?.toInstant()
        val nextCursorId = items.nextCursorId

        val encodedCursor = encodeCursor(nextCursorCreatedAt, nextCursorId)

        return QueryResult(
            items = items.items,
            summary = PaymentSummary(
                count = summaryProjection.count,
                totalAmount = summaryProjection.totalAmount,
                totalNetAmount = summaryProjection.totalNetAmount,
            ),
            nextCursor = encodedCursor,
            hasNext = items.hasNext,
        )
    }

    /** 다음 페이지 이동을 위한 커서 인코딩. */
    private fun encodeCursor(createdAt: Instant?, id: Long?): String? {
        if (createdAt == null || id == null) return null
        val raw = "${createdAt.toEpochMilli()}:$id"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray())
    }

    /** 요청으로 전달된 커서 복원. 유효하지 않으면 null 커서로 간주합니다. */
    private fun decodeCursor(cursor: String?): Pair<Instant?, Long?> {
        if (cursor.isNullOrBlank()) return null to null
        return try {
            val raw = String(Base64.getUrlDecoder().decode(cursor))
            val parts = raw.split(":")
            val ts = parts[0].toLong()
            val id = parts[1].toLong()
            Instant.ofEpochMilli(ts) to id
        } catch (e: Exception) {
            null to null
        }
    }
}
