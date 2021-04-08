package org.ossiaustria.amigo.platform.config.security

import org.ossiaustria.amigo.platform.config.censor
import org.ossiaustria.amigo.platform.services.auth.AuthService
import org.ossiaustria.amigo.platform.security.APSessionRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session


@Configuration
class AuthenticationProvider(
    val authService: AuthService,
    val sessionRegistry: APSessionRegistry,
    val sessionRepository: FindByIndexNameSessionRepository<out Session>
) : AbstractUserDetailsAuthenticationProvider() {

    companion object {
        private val log = LoggerFactory.getLogger(AuthenticationProvider::class.java)

        private val EMPTY_TOKEN_NAME = "NONE_PROVIDED"
    }


    /**
     * Will be called during EACH Request to load or reload Authentication via Service or from Session
     */
    override fun retrieveUser(accessToken: String?, authentication: UsernamePasswordAuthenticationToken?): UserDetails {
        if (accessToken == null || accessToken == EMPTY_TOKEN_NAME) {
            return authService.createGuestDetails()
        }
        if (authentication == null) {
            throw BadCredentialsException("authentication is null during AuthenticationProvider")
        }

        val fromSession: UserDetails? = sessionRegistry.retrieveFromSession(accessToken)
        if (fromSession != null) {
            log.debug("Using token details for user ${fromSession.username} from session")
            return fromSession
        }
        val fromCache: UserDetails? = this.userCache.getUserFromCache(accessToken)
        if (fromCache != null) {
            return fromCache
        }

        log.debug("!!!!!!!!!No session found for token ${accessToken.censor()}!!!!!!!!")

        throw BadCredentialsException("Token not found in Gitlab!")
//       val account = authService.findAccountByGitlabId(gitlabUser.id) ?: authService.findAccountByToken(accessToken)
//        val tokenDetails = authService.createTokenDetails(accessToken, account, gitlabUser)
//
//        if (!tokenDetails.valid) {
//            throw BadCredentialsException("Token not found in Gitlab!")
//        } else {
//            // store user in cache! This method will not be called if cache can be used!
//            // Session is being stored in cache by SessionStrategy (see org.ossiaustria.amigo.platform.config.http.RedisSessionStrategy class)
//            return tokenDetails
//        }
    }

//    private fun authenticateInGitlab(token: String): GitlabUser? {
//        try {
//            return authService.checkUserInGitlab(token)
//        } catch (ex: Exception) {
//            return null
//        }
//    }

    override fun additionalAuthenticationChecks(
        userDetails: UserDetails?,
        authentication: UsernamePasswordAuthenticationToken?
    ) =
        Unit
}
