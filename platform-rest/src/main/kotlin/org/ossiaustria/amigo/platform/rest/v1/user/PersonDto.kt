package org.ossiaustria.amigo.platform.rest.v1.user

import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import java.util.UUID

data class PersonDto(
    val id: UUID,
    val name: String,
    val groupId: UUID?,
    val memberType: MembershipType,
    val avatarUrl: String? = null
)

internal fun Person.toDto() = PersonDto(
    id = this.id,
    name = this.name,
    groupId = this.groupId,
    memberType = this.memberType,
    avatarUrl = this.avatarUrl,
)
