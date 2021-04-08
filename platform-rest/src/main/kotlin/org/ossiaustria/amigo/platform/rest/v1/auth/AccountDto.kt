package org.ossiaustria.amigo.platform.rest.v1.auth

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Person
import java.time.ZonedDateTime
import java.util.*


data class AccountDto(
    val id: UUID,
    val email: String,
    val persons: List<Person> = listOf(),
)

internal fun Account.toDto() = AccountDto(
    id = this.id,
    email = this.email,
    persons = this.persons,
)

data class SecretAccountDto(
    val id: UUID,
    val email: String,
    val jwtToken: String?,
    val jwtTokenCreatedAt: ZonedDateTime?,
    val changeAccountToken: String?,
    val changeAccountTokenCreatedAt: ZonedDateTime?,
    val persons: List<Person> = listOf(),
)

internal fun Account.toSecretUserDto() = SecretAccountDto(
    id = this.id,
    email = this.email,
    jwtToken = this.jwtToken,
    jwtTokenCreatedAt = this.jwtTokenCreatedAt,
    changeAccountToken = this.changeAccountToken,
    changeAccountTokenCreatedAt = this.changeAccountTokenCreatedAt,
    persons = this.persons,
)