package org.ossiaustria.amigo.platform.config.security

import org.ossiaustria.amigo.platform.config.http.RedisSessionStrategy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.util.matcher.AndRequestMatcher
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session

@Configuration
@EnableWebSecurity//(debug = true)
class SecurityConfiguration(private val provider: AuthenticationProvider) : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var sessionRepo: FindByIndexNameSessionRepository<out Session>

    init {
        log.debug("security configuration processing...")
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(provider)
    }

    override fun configure(webSecurity: WebSecurity) {
        webSecurity
            .ignoring()
            .antMatchers(
                "/",
                "/docs",
                "/docs/*",
                AUTH_LOGIN_URL,
                AUTH_REGISTER_URL,
            )
    }

    @Throws(Exception::class)
    public override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity
            .exceptionHandling().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .anonymous().and()
            .authorizeRequests().antMatchers("/docs", "/docs/*", AUTH_LOGIN_URL, AUTH_REGISTER_URL).permitAll()
           .and()
            .authorizeRequests().anyRequest().fullyAuthenticated()
            .and()
            .authenticationProvider(provider).addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter::class.java)
            .csrf().disable()
            .httpBasic().disable()
            .logout().disable()
    }

    @Bean
    fun authenticationFilter(): GitlabTokenAuthenticationFilter {
        val filter = GitlabTokenAuthenticationFilter(PROTECTED_MATCHER)
        filter.setAuthenticationManager(authenticationManager())
        filter.setSessionAuthenticationStrategy(sessionStrategy())
        return filter
    }

    @Bean
    fun sessionStrategy(): SessionAuthenticationStrategy {
        return RedisSessionStrategy(sessionRepo)
    }

    @Bean
    fun forbiddenEntryPoint(): AuthenticationEntryPoint {
        return HttpStatusEntryPoint(HttpStatus.FORBIDDEN)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SecurityConfiguration::class.java)

        private const val PROTECTED_URL = "/v1/**"
        private const val AUTH_LOGIN_URL = "/v1/auth/login"
        private const val AUTH_REGISTER_URL = "/v1/auth/register"

        private val PROTECTED_MATCHER = AndRequestMatcher(
            AntPathRequestMatcher(PROTECTED_URL),
            NegatedRequestMatcher(AntPathRequestMatcher(AUTH_LOGIN_URL)),
            NegatedRequestMatcher(AntPathRequestMatcher(AUTH_REGISTER_URL)),
        )
    }
}
