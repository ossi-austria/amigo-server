package org.ossiaustria.amigo.platform.domain.services.sendables

sealed class SendableError(message: String?) : Exception(message) {
    class PersonsAreTheSame : SendableError("Persons are the same")
    class PersonsNotInSameGroup : SendableError("Persons are not in the same group")
    class PersonsNotProvided : SendableError("Persons are not given")
}