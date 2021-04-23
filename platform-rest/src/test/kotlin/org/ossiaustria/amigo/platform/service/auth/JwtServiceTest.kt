package org.ossiaustria.amigo.platform.service.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.config.security.JwtService
import org.ossiaustria.amigo.platform.rest.v1.TestTags
import org.ossiaustria.amigo.platform.service.AbstractServiceTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Rollback
import java.util.UUID.randomUUID
import javax.transaction.Transactional

class JwtServiceTest : AbstractServiceTest() {

    @Autowired
    private lateinit var jwtService: JwtService

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
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

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `should generate accessToken with subject=email`() {
        val email = "test@example.org"
        val token = jwtService.generateAccessToken(randomUUID(), email, personsIds = listOf())

        assertEquals(email, token.subject)

        jwtService.validateAccessToken(token.token)
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `should generate valid accessToken`() {
        val email = "test@example.org"
        val token = jwtService.generateAccessToken(randomUUID(), email, personsIds = listOf())

        jwtService.validateAccessToken(token.token)
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `should generate refreshToken with subject=email`() {
        val email = "test@example.org"
        val token = jwtService.generateRefreshToken(randomUUID(), email)

        assertEquals(email, token.subject)

        jwtService.validateRefreshToken(token.token)
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `should generate valid refreshToken`() {
        val email = "test@example.org"
        val token = jwtService.generateRefreshToken(randomUUID(), email)

        jwtService.validateRefreshToken(token.token)
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `should retrieve claims of accessToken`() {
        val email = "test@example.org"
        val token = jwtService.generateAccessToken(randomUUID(), email, personsIds = listOf())

        val claims = jwtService.getAccessClaims(token.token)
        assertEquals("AMIGO-PLATFORM", claims.issuer)
        assertEquals("test@example.org", claims.subject)
        assertEquals(token.issuedAt, claims.issuedAt)
        assertEquals(token.expiration, claims.expiration)
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `should retrieve claims of refreshToken`() {
        val email = "test@example.org"
        val token = jwtService.generateRefreshToken(randomUUID(), email)

        val claims = jwtService.getRefreshClaims(token.token)
        assertEquals("AMIGO-PLATFORM", claims.issuer)
        assertEquals("test@example.org", claims.subject)
        assertEquals(token.issuedAt, claims.issuedAt)
        assertEquals(token.expiration, claims.expiration)
    }
}