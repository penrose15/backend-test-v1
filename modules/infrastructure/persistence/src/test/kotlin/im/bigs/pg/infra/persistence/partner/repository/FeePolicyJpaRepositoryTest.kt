package im.bigs.pg.infra.persistence.partner.repository

import im.bigs.pg.infra.persistence.config.JpaConfig
import im.bigs.pg.infra.persistence.partner.entity.FeePolicyEntity
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import java.math.BigDecimal
import java.time.Instant

@ActiveProfiles("test")
@DataJpaTest
@ContextConfiguration(classes = [JpaConfig::class])
class FeePolicyJpaRepositoryTest @Autowired constructor(
    val feePolicyJpaRepository: FeePolicyJpaRepository,
    val em: EntityManager
) {

    @Test
    @DisplayName("오늘 날짜 기준으로 가장 최신 정책 조회")
    fun `오늘 날짜 기준으로 가장 최신 정책 조회`() {
        var baseTs = Instant.parse("2025-01-01T00:00:00Z")
        val expectedPercent = BigDecimal("0.155")
        val expectedFixedFee = BigDecimal("55")
        repeat(2) { i ->
            repeat(10) { j ->
                if (j < 9) {
                    feePolicyJpaRepository.save(
                        FeePolicyEntity(
                            partnerId = (i + 1).toLong(),
                            effectiveFrom = baseTs.plusSeconds(j.toLong()),
                            percentage = BigDecimal(0.01 * j),
                            fixedFee = BigDecimal(j)
                        )
                    )
                } else {
                    feePolicyJpaRepository.save(
                        FeePolicyEntity(
                            partnerId = (i + 1).toLong(),
                            effectiveFrom = baseTs.plusSeconds(j.toLong()),
                            percentage = expectedPercent,
                            fixedFee = expectedFixedFee
                        )
                    )
                }
            }
            baseTs = Instant.parse("2025-01-01T00:00:00Z")
        }

        em.flush()
        em.clear()

        val feePolicy = feePolicyJpaRepository.findTop1ByPartnerIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            1L,
            Instant.now()
        )
        val actualPercentage = feePolicy?.percentage
        val actualFixedFee = feePolicy?.fixedFee

        Assertions.assertThat(actualPercentage?.compareTo(expectedPercent)).isEqualTo(0)
        Assertions.assertThat(actualFixedFee?.compareTo(expectedFixedFee)).isEqualTo(0)
    }

    @Test
    @DisplayName("특정 날짜 기준 가장 최신 정책 조회")
    fun `특정 날짜 기준 가장 최신 정책 조회`() {
        var baseTs = Instant.parse("2025-01-01T00:00:00Z")

        val testTs = Instant.parse("2025-01-01T00:00:00Z").plusSeconds(5.toLong())
        val expectedPercent = BigDecimal("0.155")
        val expectedFixedFee = BigDecimal("55")
        repeat(2) { i ->
            repeat(10) { j ->
                if (j != 5) {
                    feePolicyJpaRepository.save(
                        FeePolicyEntity(
                            partnerId = (i + 1).toLong(),
                            effectiveFrom = baseTs.plusSeconds(j.toLong()),
                            percentage = BigDecimal(0.01 * j),
                            fixedFee = BigDecimal(j)
                        )
                    )
                } else {
                    feePolicyJpaRepository.save(
                        FeePolicyEntity(
                            partnerId = (i + 1).toLong(),
                            effectiveFrom = baseTs.plusSeconds(j.toLong()),
                            percentage = expectedPercent,
                            fixedFee = expectedFixedFee
                        )
                    )
                }
            }
            baseTs = Instant.parse("2025-01-01T00:00:00Z")
        }

        em.flush()
        em.clear()

        val feePolicy =
            feePolicyJpaRepository.findTop1ByPartnerIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(1L, testTs)
        val actualPercentage = feePolicy?.percentage
        val actualFixedFee = feePolicy?.fixedFee

        Assertions.assertThat(actualPercentage?.compareTo(expectedPercent)).isEqualTo(0)
        Assertions.assertThat(actualFixedFee?.compareTo(expectedFixedFee)).isEqualTo(0)
    }
}
