package im.bigs.pg.external.pg.dto

data class TestPgFailureResponse(
    val code: Int? = null,
    val errorCode: String? = null,
    val message: String,
    val referenceId: String? = null
)
