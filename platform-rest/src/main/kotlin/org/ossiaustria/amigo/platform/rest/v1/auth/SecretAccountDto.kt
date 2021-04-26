package org.ossiaustria.amigo.platform.rest.v1.auth

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Person
import java.time.ZonedDateTime
import java.util.*

data class SecretAccountDto(
    val id: UUID,
    val email: String,
    val changeAccountToken: String?,
    val changeAccountTokenCreatedAt: ZonedDateTime?,
    val persons: List<PersonDto> = listOf(),
)

internal fun Account.toSecretUserDto() = SecretAccountDto(
    id = this.id,
    email = this.email,
    changeAccountToken = this.changeAccountToken,
    changeAccountTokenCreatedAt = this.changeAccountTokenCreatedAt,
    persons = this.persons.map(Person::toDto),
)
