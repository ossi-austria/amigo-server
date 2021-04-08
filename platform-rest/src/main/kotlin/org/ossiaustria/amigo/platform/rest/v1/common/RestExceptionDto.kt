package org.ossiaustria.amigo.platform.rest.v1.common

import org.ossiaustria.amigo.platform.exceptions.RestException
import java.time.ZonedDateTime

data class RestExceptionDto(
    val errorCode: Int,
    val errorName: String,
    val errorMessage: String,
    val time: ZonedDateTime = ZonedDateTime.now()
) {
    constructor(
        restException: RestException,
        time: ZonedDateTime = ZonedDateTime.now()
    ) : this(
        restException.errorCode,
        restException.errorName,
        restException.message.orEmpty(),
        time
    )
}