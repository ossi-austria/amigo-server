package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.exceptions.RestException

open class ServiceError(errorCode: Int, errorName: String, message: String, cause: Throwable?) :
    RestException(errorCode, errorName, message, cause) {

    constructor(errorName: String, cause: Exception) : this(400, errorName, cause.localizedMessage, cause)
    constructor(errorName: String, message: String, cause: Throwable?) : this(400, errorName, message, cause)
    constructor(errorName: String, message: String) : this(400, errorName, message, null)
}