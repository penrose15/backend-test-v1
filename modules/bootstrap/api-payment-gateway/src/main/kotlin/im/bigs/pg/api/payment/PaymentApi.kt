package im.bigs.pg.api.payment

import im.bigs.pg.api.config.ErrorResponse
import im.bigs.pg.api.payment.dto.CreatePaymentRequest
import im.bigs.pg.api.payment.dto.PaymentResponse
import im.bigs.pg.api.payment.dto.QueryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDateTime

interface PaymentApi {
    @Operation(summary = "결제 생성")
    @ApiResponses(
        ApiResponse(description = "성공", responseCode = "200"),
        ApiResponse(
            description = "유효하지 않은 입력값",
            responseCode = "400",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            description = "결제 승인 오류",
            responseCode = "422",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            description = "내부 서버 오류",
            responseCode = "500",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun create(@Valid @RequestBody req: CreatePaymentRequest): ResponseEntity<PaymentResponse>

    @Operation(summary = "결제 조회 (필터링 + 커서 페이지네이션)")
    fun query(
        @Parameter(description = "제휴사 식별자") @RequestParam(required = false) partnerId: Long?,
        @Parameter(description = "결제 상태") @RequestParam(required = false) status: String?,
        @Parameter(description = "시작 날짜") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") from: LocalDateTime?,
        @Parameter(description = "끝 날짜") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") to: LocalDateTime?,
        @Parameter(description = "커서 페이지네이션") @RequestParam(required = false) cursor: String?,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") limit: Int,
    ): ResponseEntity<QueryResponse>
}
