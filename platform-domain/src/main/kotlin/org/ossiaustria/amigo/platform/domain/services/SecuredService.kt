package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

sealed class SecurityError(errorName: String, message: String) : ServiceError(errorName, message, null) {

    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    class PersonHasInsufficientRights(type: MembershipType) :
        ServiceError("PERSON_RIGHTS_INSUFFICIENT", "Person needs more rights for that request, at least $type")

    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    class PersonNotFound(info: String) :
        ServiceError("PERSON_NOT_FOUND", "Person could not be found: $info")
}

open class SecuredService {

    protected fun checkPermission(person: Person, group: Group, type: MembershipType): Boolean {
        val personInGroup = group.members.find { it.id == person.id }
        return personInGroup?.memberType?.isAtLeast(type) ?: false
    }

    protected fun assertPermission(person: Person, group: Group, type: MembershipType) {
        if (!checkPermission(person, group, type)) {
            throw SecurityError.PersonHasInsufficientRights(type)
        }
    }
}
