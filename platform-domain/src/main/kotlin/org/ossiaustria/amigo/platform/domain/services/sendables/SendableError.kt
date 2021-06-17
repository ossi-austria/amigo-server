package org.ossiaustria.amigo.platform.domain.services.sendables

import org.ossiaustria.amigo.platform.domain.services.ServiceError

sealed class SendableError(errorName: String, message: String) : ServiceError(errorName, message, null) {
    class PersonsAreTheSame :
        SendableError("PERSONS_ARE_SAME", "Sender and receiver must be different for this Sendable")

    class PersonsNotInSameGroup :
        SendableError("PERSONS_NOT_IN_SAME_GROUP", "Sender and receiver are not in the same group")

    class PersonsNotProvided : SendableError("PERSONS_NOT_GIVEN", "Sender and receiver are not given")
}