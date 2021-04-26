package org.ossiaustria.amigo.platform.rest.v1.groups

import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.rest.v1.auth.PersonDto
import org.ossiaustria.amigo.platform.rest.v1.auth.toDto
import java.util.*

data class GroupDto(
    val id: UUID,
    val name: String,
    val members: List<PersonDto>,
)

internal fun Group.toDto(): GroupDto = GroupDto(
    id = this.id,
    name = this.name,
    members = this.members.map(Person::toDto),
)