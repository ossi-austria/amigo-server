package org.ossiaustria.amigo.platform.config.security

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthTokenFilter() : OncePerRequestFilter() {

    private lateinit var authenticationManager: AuthenticationManager

    companion object {
        private val log = LoggerFactory.getLogger(AuthTokenFilter::class.java)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        handleTokenAuthentication(request)
        filterChain.doFilter(request, response)
    }

    private fun handleTokenAuthentication(request: HttpServletRequest?) {
        val token = request?.getHeader("Authorization")?.removePrefix("Bearer")?.trim()
        if (token.isNullOrBlank()) {
            return
        }

        try {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(token, token, authorities)
            )
            if (!authentication.isAuthenticated) {
                throw BadCredentialsException("Not authenticated")
            }
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setAuthenticationManager(authenticationManager: AuthenticationManager) {
        this.authenticationManager = authenticationManager
    }


}