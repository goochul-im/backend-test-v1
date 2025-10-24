package im.bigs.pg.external.pg.exception

import im.bigs.pg.common.exception.BusinessException
import im.bigs.pg.common.exception.ErrorCode

class PgClientException(message: String) : BusinessException(ErrorCode.PG_CLIENT_EXCEPTION, message)
