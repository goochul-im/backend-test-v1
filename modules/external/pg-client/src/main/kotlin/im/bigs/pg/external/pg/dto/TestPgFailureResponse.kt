package im.bigs.pg.external.pg.dto

data class TestPgFailureResponse(
    val code: Int,
    val errorCode: String,
    val message: String,
    val referenceId: String
)
