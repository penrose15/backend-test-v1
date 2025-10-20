package im.bigs.pg.api.payment

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.api.payment.dto.CreatePaymentRequest
import im.bigs.pg.api.payment.dto.PaymentResponse
import im.bigs.pg.api.payment.dto.QueryResponse
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.infra.persistence.payment.entity.PaymentEntity
import im.bigs.pg.infra.persistence.payment.repository.PaymentJpaRepository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.InstanceOfAssertFactories
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentControllerTest @Autowired constructor(
    private val mvcTester: MockMvcTester,
    private val objectMapper: ObjectMapper,
    private val em: EntityManager,
    private val paymentJpaRepository: PaymentJpaRepository
) {

    @Test
    fun create() {
        val request = CreatePaymentRequest(
            partnerId = 1L,
            amount = 1000.00.toBigDecimal(),
            cardBin = "111111",
            cardLast4 = "1111",
            birthDate = "19900101",
            expiry = "1227",
            password = "12",
            productName = "Test Product"
        )
        val requestJson = objectMapper.writeValueAsString(request)
        val result = mvcTester.post()
            .uri("/api/v1/payments")
            .contentType("application/json")
            .content(requestJson).exchange()

        assertThat(result)
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.id") { assertThat(it).isNotNull() }
            .hasPathSatisfying("$.approvalCode") { assertThat(it).isNotNull() }

        val paymentResponse = objectMapper.readValue(result.response.contentAsString, PaymentResponse::class.java)
        val payment = paymentJpaRepository.findById(paymentResponse.id!!).orElseThrow()

        assertThat(payment.partnerId).isEqualTo(request.partnerId)
        assertThat(payment.id).isEqualTo(paymentResponse.id)
        assertThat(payment.amount).isEqualTo(paymentResponse.amount)
        assertThat(payment.netAmount).isEqualTo(paymentResponse.netAmount)
    }

    @Test
    fun queryWithNullCursor() {
        // generate test payments
        generateTestPayments()

        val requestParams = getRequestParams(null)

        val result = mvcTester.get()
            .uri("/api/v1/payments")
            .params(requestParams)
            .contentType("application/json")
            .exchange()

        assertThat(result)
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.items") {
                assertThat(it)
                    .asInstanceOf(InstanceOfAssertFactories.LIST)
                    .hasSize(20)
            }
            .hasPathSatisfying("$.summary.count") {
                assertThat(it).isEqualTo(25)
            }
            // 1000.00 * 25 = 25000.00
            .hasPathSatisfying("$.summary.totalAmount") {
                assertThat(it).isEqualTo(25000)
            }
            .hasPathSatisfying("$.nextCursor") {
                assertThat(it).isNotNull()
            }
            .hasPathSatisfying("$.hasNext") {
                assertThat(it).isEqualTo(true)
            }
    }

    @Test
    fun queryWithCursor() {
        // generate test payments
        generateTestPayments()

        val firstReqParams = getRequestParams(null)

        val initialResult = mvcTester.get()
            .uri("/api/v1/payments")
            .params(firstReqParams)
            .contentType("application/json")
            .exchange()

        val nextCursor = objectMapper.readValue(initialResult.response.contentAsString, QueryResponse::class.java)
            .nextCursor

        val requestParams = getRequestParams(nextCursor)

        val result = mvcTester.get()
            .uri("/api/v1/payments")
            .params(requestParams)
            .contentType("application/json")
            .exchange()

        assertThat(result)
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.items") {
                assertThat(it)
                    .asInstanceOf(InstanceOfAssertFactories.LIST)
                    .hasSize(5)
            }
            .hasPathSatisfying("$.summary.count") {
                assertThat(it).isEqualTo(25)
            }
            // 1000.00 * 25 = 25000.00
            .hasPathSatisfying("$.summary.totalAmount") {
                assertThat(it).isEqualTo(25000)
            }
            .hasPathSatisfying("$.hasNext") {
                assertThat(it).isEqualTo(false)
            }
    }

    private fun getRequestParams(cursor: String?): LinkedMultiValueMap<String, String> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val from = formatter.format(LocalDateTime.now().minusDays(1))
        val to = formatter.format(LocalDateTime.now())

        val requestParams = LinkedMultiValueMap<String, String>()
        requestParams.add("partnerId", "1")
        requestParams.add("status", PaymentStatus.APPROVED.name)
        requestParams.add("from", from)
        requestParams.add("to", to)
        requestParams.add("cursor", cursor)
        requestParams.add("limit", "20")
        return requestParams
    }

    private fun generateTestPayments() {
        val payments = (1..25).map {
            PaymentEntity(
                partnerId = 1L,
                amount = 1000.00.toBigDecimal(),
                appliedFeeRate = 0.03.toBigDecimal(),
                feeAmount = 30.00.toBigDecimal(),
                netAmount = 970.00.toBigDecimal(),
                cardBin = "123456",
                cardLast4 = "7890",
                approvalCode = "",
                status = PaymentStatus.APPROVED.name,
                approvedAt = Instant.now().minusSeconds(3600),
                createdAt = Instant.now().minusSeconds(3600).plusSeconds(it.toLong()),
            )
        }
        paymentJpaRepository.saveAll(payments)
        em.flush()
        em.clear()
    }
}
