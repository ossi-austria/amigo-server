package org.ossiaustria.amigo.platform.rest.v1.common

import org.ossiaustria.amigo.platform.exceptions.ValidationException
import org.springframework.validation.FieldError
import java.time.LocalDateTime

class ValidationFailureDto(
    val errorCode: Int,
    val errorName: String,
    val errorMessage: String,
    val validationErrors: Array<FieldError?>,
    val time: LocalDateTime = LocalDateTime.now()
) {
    constructor(
        restException: ValidationException,
        time: LocalDateTime = LocalDateTime.now()
    ) : this(
        restException.errorCode,
        restException.errorName,
        restException.message.orEmpty(),
        restException.validationErrors,
        time
    )
}