package im.bigs.pg.api.config

import im.bigs.pg.domain.partner.exception.FeePolicyNotFoundException
import im.bigs.pg.external.pg.exception.PayFailedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiControllerAdvice {

    // PG 결제 실패 예외 처리
    @ExceptionHandler(PayFailedException::class)
    fun handlePayFailedException(ex: PayFailedException): ResponseEntity<ErrorResponse> {
        return getErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.message, ex)
    }

    @ExceptionHandler(FeePolicyNotFoundException::class)
    fun handleFeePolicyException(ex: FeePolicyNotFoundException): ResponseEntity<ErrorResponse> {
        return getErrorResponse(HttpStatus.NOT_FOUND, ex.message, ex)
    }

    // validation error
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBindException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val result = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value")
        }
        return getErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed: $result", ex)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return getErrorResponse(HttpStatus.BAD_REQUEST, ex.message, ex)
    }

    @ExceptionHandler(java.lang.Exception::class)
    fun handleException(ex: java.lang.Exception): ResponseEntity<ErrorResponse> {
        return getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.message, ex)
    }

    private fun getErrorResponse(code: HttpStatus, message: String?, ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(code.value(), message, ex.javaClass.simpleName)

        return ResponseEntity.status(code).body(errorResponse)
    }
}
