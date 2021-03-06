package org.ossiaustria.amigo.platform.domain.services.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.config.Constants
import org.ossiaustria.amigo.platform.domain.services.AbstractServiceTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Rollback
import java.util.UUID.randomUUID

internal class JwtServiceTest : AbstractServiceTest() {

    @Autowired
    private lateinit var jwtService: JwtService

    @Rollback
    @Test
    fun `should generate not-null accessToken`() {
        val email = "test@example.org"
        val token = jwtService.generateAccessToken(randomUUID(), email, personsIds = listOf())

        assertNotNull(token)
        assertNotNull(token.token)
        assertNotNull(token.subject)
        assertNotNull(token.issuedAt)
        assertNotNull(token.expiration)
        assertNotNull(token.issuedAt)
    }

    @Rollback
    @Test
    fun `should generate accessToken with subject=email`() {
        val email = "test@example.org"
        val token = jwtService.generateAccessToken(randomUUID(), email, personsIds = listOf())

        assertEquals(email, token.subject)

        jwtService.validateAccessToken(token.token)
    }

    @Rollback
    @Test
    fun `should generate valid accessToken`() {
        val email = "test@example.org"
        val token = jwtService.generateAccessToken(randomUUID(), email, personsIds = listOf())

        jwtService.validateAccessToken(token.token)
    }

    @Rollback
    @Test
    fun `should generate refreshToken with subject=email`() {
        val email = "test@example.org"
        val token = jwtService.generateRefreshToken(randomUUID(), email)

        assertEquals(email, token.subject)

        jwtService.validateRefreshToken(token.token)
    }

    @Rollback
    @Test
    fun `should generate valid refreshToken`() {
        val email = "test@example.org"
        val token = jwtService.generateRefreshToken(randomUUID(), email)

        jwtService.validateRefreshToken(token.token)
    }

    @Rollback
    @Test
    fun `should retrieve claims of accessToken`() {
        val email = "test@example.org"
        val token = jwtService.generateAccessToken(randomUUID(), email, personsIds = listOf())

        val claims = jwtService.getAccessClaims(token.token)
        assertEquals(Constants.JWT_ISSUER, claims.issuer)
        assertEquals("test@example.org", claims.subject)
        assertEquals(token.issuedAt, claims.issuedAt)
        assertEquals(token.expiration, claims.expiration)
    }

    @Rollback
    @Test
    fun `should retrieve claims of refreshToken`() {
        val email = "test@example.org"
        val token = jwtService.generateRefreshToken(randomUUID(), email)

        val claims = jwtService.getRefreshClaims(token.token)
        assertEquals(Constants.JWT_ISSUER, claims.issuer)
        assertEquals("test@example.org", claims.subject)
        assertEquals(token.issuedAt, claims.issuedAt)
        assertEquals(token.expiration, claims.expiration)
    }
}