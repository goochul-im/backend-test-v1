package im.bigs.pg.external.pg.security

import im.bigs.pg.application.pg.port.out.PgApproveRequest

interface PgEncryptor {

    fun encrypt(plainText: PgApproveRequest): String

}
