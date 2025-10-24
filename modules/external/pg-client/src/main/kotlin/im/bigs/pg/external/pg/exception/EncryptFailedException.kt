package im.bigs.pg.external.pg.exception

import im.bigs.pg.common.exception.BusinessException
import im.bigs.pg.common.exception.ErrorCode


class EncryptFailedException(string: String) : BusinessException(ErrorCode.ENCRYPTION_ERROR, string)
