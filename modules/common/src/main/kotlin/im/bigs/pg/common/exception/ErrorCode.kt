package im.bigs.pg.common.exception

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ErrorCode(
    val code: String,
    val message: String
) {

    ENCRYPTION_ERROR("E001", "Encrypt Error"),
    PG_CLIENT_EXCEPTION("E002", "PG Client Exception"),
    INVALID_INPUT_VALUE("E003", "Invalid Input Value"),
    ENTITY_NOT_FOUND("E004", "Entity Not Found"),
    PAYMENT_EXCEPTION("E005", "Partner Not Found"),

}
