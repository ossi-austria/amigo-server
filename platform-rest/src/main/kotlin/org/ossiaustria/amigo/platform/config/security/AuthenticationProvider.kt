package org.ossiaustria.amigo.platform.config.security

import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
import org.springframework.security.core.userdetails.UserDetails
import java.util.Date


@Configuration
class AuthenticationProvider(
    private val authService: AuthService,
) : AbstractUserDetailsAuthenticationProvider() {

    companion object {
        private const val EMPTY_TOKEN_NAME = "NONE_PROVIDED"
    }

    /**
     * Will be called during EACH Request to load or reload Authentication via Service or from Session
     */
    override fun retrieveUser(accessToken: String?, authentication: UsernamePasswordAuthenticationToken?): UserDetails {
        if (accessToken == null || accessToken == EMPTY_TOKEN_NAME) {
            return createGuestDetails()
        }
        if (authentication == null) {
            throw BadCredentialsException("authentication is null during AuthenticationProvider")
        }

        this.userCache.getUserFromCache(accessToken)?.let { return it }

        val jwtToken = authentication.credentials as String
        return authService.checkValidAccessToken(jwtToken)
    }

    override fun additionalAuthenticationChecks(
        userDetails: UserDetails?,
        authentication: UsernamePasswordAuthenticationToken?
    ) = Unit

    fun createGuestDetails(): TokenUserDetails {
        return GUEST_TOKEN_USER_DETAILS
    }

    private val GUEST_TOKEN_USER_DETAILS: TokenUserDetails by lazy {
        TokenUserDetails(
            email = "",
            accountId = AuthService.GUEST_ACCOUNT_ID,
            personsIds = listOf(),
            expiration = Date(),
            issuedAt = Date(),
        )
    }
}
