package org.ossiaustria.amigo.platform.rest.v1.user

import io.swagger.annotations.ApiModel
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Person
import java.util.UUID


@ApiModel("Account which holds own Persons and technical authentication data")
data class AccountDto(
    val id: UUID,
    val email: String,
    val persons: List<PersonDto> = listOf(),
)

internal fun Account.toDto() = AccountDto(
    id = this.id,
    email = this.email,
    persons = this.persons.map(Person::toDto),
)




