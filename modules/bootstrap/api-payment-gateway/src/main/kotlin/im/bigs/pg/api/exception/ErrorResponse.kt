package im.bigs.pg.api.exception

import im.bigs.pg.common.exception.ErrorCode
import org.springframework.validation.BindingResult

class ErrorResponse(
    errorCode: ErrorCode,
    message: String? = null,
    var errors: List<FieldError> = ArrayList()
) {

    var code: String = errorCode.code
        private set

    var message: String = message ?: errorCode.message
        private set

    class FieldError private constructor(
        val filed: String,
        val value: String,
        val reason: String?
    ) {
        companion object {

            fun of(bindingResult: BindingResult): List<FieldError> {
                val fieldErrors = bindingResult.fieldErrors

                return fieldErrors.map { fieldError ->
                    FieldError(
                        filed = fieldError.field,
                        value = fieldError.rejectedValue?.toString() ?: "",
                        reason = fieldError.defaultMessage
                    )
                }
            }

        }
    }

    companion object {

        fun of(code: ErrorCode, bindingResult: BindingResult): ErrorResponse {
            return ErrorResponse(
                errorCode = code,
                errors = FieldError.of(bindingResult)
            )
        }

        fun of(code: ErrorCode): ErrorResponse {
            return ErrorResponse(
                errorCode = code
            )
        }

        fun of(code: ErrorCode, message: String?): ErrorResponse {
            return ErrorResponse(
                errorCode = code,
                message = message
            )
        }
    }
}
