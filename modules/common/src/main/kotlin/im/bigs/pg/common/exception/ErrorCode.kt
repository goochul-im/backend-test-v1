package im.bigs.pg.common.exception

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ErrorCode(
    val code: String,
    val message: String
) {

    ENCRYPTION_ERROR("E001", "Encrypt Error"),

}
