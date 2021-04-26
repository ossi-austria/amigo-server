package org.ossiaustria.amigo.platform.rest.v1.auth

import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import java.util.*


data class PersonDto(
    val id: UUID,
    val name: String,
    val groupId: UUID?,
    val memberType: MembershipType,
)

internal fun Person.toDto() = PersonDto(
    id = this.id,
    name = this.name,
    groupId = this.groupId,
    memberType = this.memberType,
)