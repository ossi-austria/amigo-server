package org.ossiaustria.amigo.platform.domain.services.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.DefaultClaims
import org.ossiaustria.amigo.platform.domain.config.Constants
import org.ossiaustria.amigo.platform.exceptions.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID


@Service
class JwtService {

    @Value("\${amigo-platform.security.accessTokenSecret}")
    private lateinit var accessTokenSecret: String

    @Value("\${amigo-platform.security.refreshTokenSecret}")
    private lateinit var refreshTokenSecret: String

    @Value("\${amigo-platform.security.accessTokenExpirationSec}")
    private val accessTokenExpirationSec = 0L

    @Value("\${amigo-platform.security.refreshTokenExpirationSec}")
    private val refreshTokenExpirationSec = 0L

    fun generateAccessToken(
        accountId: UUID,
        email: String,
        lifeTimeSec: Long = accessTokenExpirationSec,
        personsIds: List<UUID>
    ): TokenResult = generateToken(accountId, email, lifeTimeSec, accessTokenSecret, personsIds)

    fun generateRefreshToken(
        accountId: UUID,
        email: String,
        lifeTimeSec: Long = refreshTokenExpirationSec
    ): TokenResult = generateToken(accountId, email, lifeTimeSec, refreshTokenSecret, null)

    protected fun generateToken(
        accountId: UUID,
        email: String,
        lifeTimeSec: Long,
        tokenSecret: String,
        personsIds: List<UUID>? = null
    ): TokenResult {

        val tokenClaims: Claims = DefaultClaims().apply {
            subject = email
            issuedAt = ZonedDateTime.now().toDate()
            expiration = ZonedDateTime.now().plusSeconds(lifeTimeSec).toDate()
            issuer = Constants.JWT_ISSUER
        }

        tokenClaims[CLAIM_ACCOUNT_ID] = accountId

        if (personsIds != null) {
            tokenClaims[CLAIM_PERSONS_IDS] = personsIds
        }

        val jwtToken = Jwts.builder()
            .setClaims(tokenClaims)
            .signWith(SignatureAlgorithm.HS512, tokenSecret)
            .compact()

        validateJwtToken(jwtToken, tokenSecret)
        return TokenResult(
            token = jwtToken,
            subject = tokenClaims.subject,
            issuedAt = tokenClaims.issuedAt,
            expiration = tokenClaims.expiration,
            issuer = tokenClaims.issuer,
        )
    }

    fun getAccessClaims(token: String) =
        Jwts.parser().setSigningKey(accessTokenSecret).parseClaimsJws(token).body as DefaultClaims

    fun getRefreshClaims(token: String) =
        Jwts.parser().setSigningKey(refreshTokenSecret).parseClaimsJws(token).body as DefaultClaims

    fun validateAccessToken(accessToken: String) = validateJwtToken(accessToken, accessTokenSecret)
    fun validateRefreshToken(refreshToken: String) = validateJwtToken(refreshToken, refreshTokenSecret)

    private fun validateJwtToken(authToken: String, tokenSecret: String?): Boolean = try {
        Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(authToken)
        true
    } catch (e: Exception) {
        log.error("JWT token cannot be used: {}", e.message)
        throw UnauthorizedException(e.message)
    }

    companion object {
        private val log = LoggerFactory.getLogger(JwtService::class.java)
        const val CLAIM_ACCOUNT_ID = "accId"
        const val CLAIM_PERSONS_IDS = "personsIds"
    }
}

fun ZonedDateTime.toDate(): Date = Date.from(this.toInstant())
