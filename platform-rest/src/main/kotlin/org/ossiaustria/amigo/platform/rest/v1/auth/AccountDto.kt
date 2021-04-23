package org.ossiaustria.amigo.platform.rest.v1.auth

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.services.auth.LoginResult
import org.ossiaustria.amigo.platform.services.auth.TokenResult
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
    val changeAccountToken: String?,
    val changeAccountTokenCreatedAt: ZonedDateTime?,
    val persons: List<Person> = listOf(),
)


internal fun Account.toSecretUserDto() = SecretAccountDto(
    id = this.id,
    email = this.email,
    changeAccountToken = this.changeAccountToken,
    changeAccountTokenCreatedAt = this.changeAccountTokenCreatedAt,
    persons = this.persons,
)

data class TokenResultDto(
    val token: String,
    val subject: String,
    val issuedAt: Date,
    val expiration: Date,
    val issuer: String
)


internal fun TokenResult.toDto() = TokenResultDto(
    token = token,
    subject = subject,
    issuedAt = issuedAt,
    expiration = expiration,
    issuer = issuer,
)

data class LoginResultDto(
    val account: SecretAccountDto,
    val refreshToken: TokenResultDto,
    val accessToken: TokenResultDto,
)

internal fun LoginResult.toDto() = LoginResultDto(
    account = account.toSecretUserDto(),
    refreshToken = refreshToken.toDto(),
    accessToken = accessToken.toDto()
)