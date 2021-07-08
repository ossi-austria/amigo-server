package org.ossiaustria.amigo.platform.domain.services.jitsi

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.DefaultClaims
import io.jsonwebtoken.impl.TextCodec
import org.ossiaustria.amigo.platform.domain.config.Constants
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.services.auth.toDate
import org.ossiaustria.amigo.platform.exceptions.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.time.ZonedDateTime


@Service
class JitsiJwtService {

    @Value("\${amigo-platform.jitsi.jwtAppId}")
    private lateinit var jwtAppId: String

    @Value("\${amigo-platform.jitsi.jwtAppSecret}")
    private lateinit var jwtAppSecret: String

    @Value("\${amigo-platform.jitsi.rootUrl}")
    private lateinit var rootUrl: String

    fun generateToken(roomName: String, selfUser: Person, otherUser: Person?): String {

        val host = URL(rootUrl).host
        val tokenClaims: Claims = DefaultClaims().apply {
            audience = jwtAppId
            issuer = Constants.JITSI_JWT_AUD_ISS
            subject = host
            issuedAt = ZonedDateTime.now().toDate()
            notBefore = ZonedDateTime.now().toDate()
            expiration = ZonedDateTime.now().plusSeconds(LIFETIME).toDate()
        }

        tokenClaims["room"] = roomName

        // optional, needed for displaying user date
        val context = hashMapOf<String, Any>(
            "user" to hashMapOf<String, Any>(
                "id" to selfUser.id,
                "name" to selfUser.name,
                "email" to "email@example.org",
                "avatar" to "https://gravatar.com/avatar/abc123",
            )
        )

        if (otherUser != null) {
            context["callee"] = hashMapOf<String, Any>(
                "id" to otherUser.id,
                "name" to otherUser.name,
                "avatar" to "https:/gravatar.com/avatar/abc123",
            )
        }
        tokenClaims["context"] = context

        val base64Encoded = TextCodec.BASE64.encode(jwtAppSecret)
        val jwtToken = Jwts.builder()
            .setHeaderParam("alg", "HS256")
            .setHeaderParam("typ", "JWT")
            .setClaims(tokenClaims)
            .signWith(SignatureAlgorithm.HS256, base64Encoded)
            .compact()

        val jwtTokenUrl = "https://$host/$roomName?jwt=$jwtToken"
        Log.info("Generated jwtToken for Call $roomName: $jwtToken")
        Log.info("Generated jwtToken url for Call $roomName: $jwtTokenUrl")
        validateJwtToken(jwtToken, base64Encoded)
        return jwtToken
    }


    private fun validateJwtToken(authToken: String, tokenSecret: String?): Boolean = try {
        Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(authToken)
        true
    } catch (e: Exception) {
        Log.error("JWT token cannot be used: {}", e.message)
        throw UnauthorizedException(e.message)
    }

    companion object {
        private val Log = LoggerFactory.getLogger(JitsiJwtService::class.java)
        const val LIFETIME = 60 * 60 * 1L
    }
}
