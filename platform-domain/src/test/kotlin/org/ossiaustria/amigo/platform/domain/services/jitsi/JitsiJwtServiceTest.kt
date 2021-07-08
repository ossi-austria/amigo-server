package org.ossiaustria.amigo.platform.domain.services.jitsi

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.impl.TextCodec
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.config.Constants
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.services.AbstractServiceTest
import org.ossiaustria.amigo.platform.domain.services.auth.toDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.annotation.Rollback
import java.net.URL
import java.time.ZonedDateTime
import java.util.UUID.randomUUID

@Suppress("UNCHECKED_CAST")
internal class JitsiJwtServiceTest : AbstractServiceTest() {

    @Autowired
    private lateinit var jitsiJwtService: JitsiJwtService

    @Value("\${amigo-platform.jitsi.jwtAppId}")
    private lateinit var jwtAppId: String

    @Value("\${amigo-platform.jitsi.jwtAppSecret}")
    private lateinit var jwtAppSecret: String

    @Value("\${amigo-platform.jitsi.rootUrl}")
    private lateinit var rootUrl: String

   private val groupId = randomUUID()

    @BeforeEach
    fun beforeEach() {
        person1 = Person(randomUUID(), randomUUID(), "person1", groupId)
        person2 = Person(randomUUID(), randomUUID(), "person2", groupId)
    }

    @Rollback
    @Test
    fun `should generate not-null jwtToken`() {
        val token = jitsiJwtService.generateToken("asdfasdf", person1, null)

        assertNotNull(token)
        parseToken(token)
    }

    @Rollback
    @Test
    fun `should set Jitsi JWT claim audience = app-id`() {
        val token = jitsiJwtService.generateToken("roomName", person1, null)
        val jwtBody = parseToken(token)
        assertEquals(jwtAppId, jwtBody.audience)
    }

    @Rollback
    @Test
    fun `should set Jitsi JWT claim issuer = AMIGO-PLATFORM`() {
        val token = jitsiJwtService.generateToken("roomName", person1, null)
        val jwtBody = parseToken(token)
        assertEquals(jwtAppId, jwtBody.audience)
        assertEquals(Constants.JITSI_JWT_AUD_ISS, jwtBody.issuer)
    }

    @Rollback
    @Test
    fun `should set Jitsi JWT claim subject = allowed url`() {
        val token = jitsiJwtService.generateToken("roomName", person1, null)
        val jwtBody = parseToken(token)
        assertEquals(URL(rootUrl).host, jwtBody.subject)
    }

    @Rollback
    @Test
    fun `should set Jitsi JWT claims issuedAt & expiration`() {
        val before = ZonedDateTime.now().minusSeconds(1)
        val token = jitsiJwtService.generateToken("roomName", person1, null)
        val after = ZonedDateTime.now().plusSeconds(1)
        val jwtBody = parseToken(token)
        val issuedAt = jwtBody.issuedAt
        val expiration = jwtBody.expiration
        assertTrue(before.toDate().before(issuedAt))
        assertTrue(after.toDate().after(issuedAt))
        assertTrue(before.plusSeconds(JitsiJwtService.LIFETIME).toDate().before(expiration))
        assertTrue(after.plusSeconds(JitsiJwtService.LIFETIME).toDate().after(expiration))
    }

    @Rollback
    @Test
    fun `should set roomName`() {
        val token = jitsiJwtService.generateToken("roomName", person1, null)
        val jwtBody = parseToken(token)
        assertEquals("roomName", jwtBody.key("room"))
    }

    @Rollback
    @Test
    fun `should set person1-user id`() {
        val token = jitsiJwtService.generateToken("roomName", person1, null)
        val jwtBody = parseToken(token)
        val inContext = jwtBody.inContext("user") as HashMap<String, *>?
        assertEquals(person1.id.toString(), inContext?.get("id"))
    }

    @Rollback
    @Test
    fun `should set person2-callee id`() {
        val token = jitsiJwtService.generateToken("roomName", person1, person2)
        val jwtBody = parseToken(token)
        val inContext = jwtBody.inContext("callee") as HashMap<String, *>?
        assertEquals(person2.id.toString(), inContext?.get("id"))
    }

    @Rollback
    @Test
    fun `should not set person2-callee id`() {
        val token = jitsiJwtService.generateToken("roomName", person1, null)
        val jwtBody = parseToken(token)
        assertEquals(null, jwtBody.inContext("callee"))
    }

    private fun parseToken(token: String) =
        Jwts.parser().setSigningKey(TextCodec.BASE64.encode(jwtAppSecret)).parseClaimsJws(token).body

    private fun Claims.key(key: String): Any? = this[key]

    @Suppress("UNCHECKED_CAST")
    private fun Claims.context(): HashMap<String, *>? =
        key("context") as HashMap<String, HashMap<String, *>>?

    private fun Claims.inContext(key: String): Any? =
        context().let { context -> context?.get(key) }
}