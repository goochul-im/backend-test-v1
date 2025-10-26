package im.bigs.pg.api.exception

import im.bigs.pg.application.exception.PaymentException
import im.bigs.pg.common.exception.ErrorCode
import im.bigs.pg.external.pg.exception.EncryptFailedException
import im.bigs.pg.external.pg.exception.PgClientException
import jakarta.persistence.EntityNotFoundException
import jakarta.persistence.NoResultException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(EncryptFailedException::class)
    fun handleEncryptFailedException(e: EncryptFailedException): ResponseEntity<ErrorResponse> {

        log.error( "EncryptFailedException: $e")

        val response = ErrorResponse(ErrorCode.ENCRYPTION_ERROR)

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(PgClientException::class)
    fun handlePgClientException(e: PgClientException): ResponseEntity<ErrorResponse> {

        log.error ( "PgClientException: $e" )

        val response = ErrorResponse(ErrorCode.PG_CLIENT_EXCEPTION)

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {

        log.error( "MethodArgumentNotValidException: $e")

        val of = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)

        return ResponseEntity(of, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {

        log.error( "HttpMessageNotReadableException: $e")

        val response = ErrorResponse(ErrorCode.INVALID_INPUT_VALUE)

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleMethodArgumentNotValidException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        log.error("IllegalArgumentException: $e")

        val of = ErrorResponse.of(ErrorCode.ENTITY_NOT_FOUND, e.message)

        return ResponseEntity(of, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleMethodArgumentNotValidException(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        log.error("IllegalStateException: $e")

        val of = ErrorResponse.of(ErrorCode.ENTITY_NOT_FOUND, e.message)

        return ResponseEntity(of, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(PaymentException::class)
    fun handlePaymentException(e: PaymentException): ResponseEntity<ErrorResponse> {
        log.error("PaymentException: $e")

        val of = ErrorResponse.of(ErrorCode.PAYMENT_EXCEPTION, e.message)

        return ResponseEntity(
            of,
            HttpStatus.BAD_REQUEST
        )
    }

}
