package org.ossiaustria.amigo.platform.rest.v1.user

import io.swagger.annotations.ApiModel
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import java.util.UUID

@ApiModel("Group with its members (normal Persons, Analogue and Admins/Owner")
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
