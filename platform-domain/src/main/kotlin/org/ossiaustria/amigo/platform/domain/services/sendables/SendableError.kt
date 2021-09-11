package org.ossiaustria.amigo.platform.domain.services.sendables

import org.ossiaustria.amigo.platform.domain.services.ServiceError
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

sealed class SendableError(errorName: String, message: String) : ServiceError(errorName, message, null) {

    @ResponseStatus(code = HttpStatus.CONFLICT)
    class PersonsAreTheSame :
        SendableError("PERSONS_ARE_SAME", "Sender and receiver must be different for this Sendable")

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    class PersonsNotInSameGroup :
        SendableError("PERSONS_NOT_IN_SAME_GROUP", "Sender and receiver are not in the same group")

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    class PersonsNotProvided : SendableError("PERSONS_NOT_GIVEN", "Sender and receiver are not given")
}