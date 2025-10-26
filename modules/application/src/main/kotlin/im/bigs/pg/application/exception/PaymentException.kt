package im.bigs.pg.application.exception

import im.bigs.pg.common.exception.BusinessException
import im.bigs.pg.common.exception.ErrorCode

class PaymentException(message: String) : BusinessException(ErrorCode.PAYMENT_EXCEPTION, message)
