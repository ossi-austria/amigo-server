package org.ossiaustria.amigo.platform.rest.v1.user

import org.ossiaustria.amigo.platform.domain.services.auth.TokenResult
import java.util.*

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