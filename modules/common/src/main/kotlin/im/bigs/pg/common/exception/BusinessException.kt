package im.bigs.pg.common.exception

open class BusinessException : RuntimeException {

    private var errorCode: ErrorCode
        get() {
            return this.errorCode
        }

    constructor(errorCode: ErrorCode) : super(errorCode.message) {
        this.errorCode = errorCode
    }

    constructor(errorCode: ErrorCode, message: String) : super(message) {
        this.errorCode = errorCode
    }
}
