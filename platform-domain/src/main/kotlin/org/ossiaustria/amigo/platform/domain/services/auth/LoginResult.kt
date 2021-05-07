package org.ossiaustria.amigo.platform.domain.services.auth

import org.ossiaustria.amigo.platform.domain.models.Account

data class LoginResult(
    val account: Account,
    val refreshToken: TokenResult,
    val accessToken: TokenResult,
)