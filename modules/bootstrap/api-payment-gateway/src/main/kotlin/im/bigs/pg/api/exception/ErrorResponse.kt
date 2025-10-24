package im.bigs.pg.api.exception

import org.springframework.validation.BindingResult
import kotlin.code

class ErrorResponse(
    errorCode: ErrorCode,
    var errors: List<FieldError> = ArrayList()
) {

    var code:String = errorCode.code
        private set

    var message:String = errorCode.message
        private set

    class FieldError private constructor(
        val filed:String,
        val value:String,
        val reason:String?
    ){
        companion object{

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

    }
}
