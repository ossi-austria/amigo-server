package org.ossiaustria.amigo.platform.rest.v1.auth

import org.ossiaustria.amigo.platform.domain.services.auth.LoginResult

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