package im.bigs.pg.api.exception

import im.bigs.pg.common.exception.ErrorCode
import im.bigs.pg.external.pg.exception.EncryptFailedException
import im.bigs.pg.external.pg.exception.PgClientException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(EncryptFailedException::class)
    fun handleEncryptFailedException(e: EncryptFailedException): ResponseEntity<ErrorResponse> {

        val response = ErrorResponse(ErrorCode.ENCRYPTION_ERROR)

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(PgClientException::class)
    fun handlePgClientException(e: PgClientException): ResponseEntity<ErrorResponse> {

        val response = ErrorResponse(ErrorCode.PG_CLIENT_EXCEPTION)

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

}
