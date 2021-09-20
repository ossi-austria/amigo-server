package org.ossiaustria.amigo.platform.config.security

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
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
import springfox.documentation.builders.ApiInfoBuilder

import springfox.documentation.builders.PathSelectors

import springfox.documentation.builders.RequestHandlerSelectors

import springfox.documentation.spi.DocumentationType

import springfox.documentation.spring.web.plugins.Docket


@Configuration
@EnableWebSecurity//(debug = true)
class SecurityConfiguration(private val provider: AuthenticationProvider) : WebSecurityConfigurerAdapter() {

    init {
        log.debug("security configuration processing...")
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(provider)
    }

    override fun configure(webSecurity: WebSecurity) {
        webSecurity.ignoring().antMatchers("/docs", "/docs/*")
        webSecurity.ignoring().antMatchers("/error", "/favicon.ico")
        webSecurity.ignoring().antMatchers("/swagger-ui/**", "/configuration/**","/swagger-ui.html",
            "/swagger-ui.html/","/swagger-ui.html/*","/swagger-ui.html/**",
            "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs", "/webjars/**")
    }

    @Throws(Exception::class)
    public override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint()).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .anonymous().and()
            .authorizeRequests()
            .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html","/swagger-ui.html/","/swagger-ui.html/*", "/webjars/swagger-ui/**").permitAll()
            .and().authorizeRequests()
            .antMatchers(
                "*",
                "/*",
                "/error",
                "/favicon.ico",
                "/actuator/**",
                AUTH_URLS,
                AUTH_LOGIN_URLS,
                AUTH_PASSWORD_URLS,
                AUTH_PASSWORD_RESET_URLS
            )
            .permitAll().and()
            .authorizeRequests().anyRequest().authenticated().and()
            .authenticationProvider(provider)
            .addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter::class.java)
            .csrf().disable()
            .httpBasic().and()
            .logout().disable()
    }

    @Bean
    fun authenticationFilter(): AuthTokenFilter = AuthTokenFilter().apply {
        setAuthenticationManager(authenticationManager())
    }

    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager = super.authenticationManagerBean()

    @Bean
    fun amigoPlatformSwaggerDocs(): Docket? {
        val version = "0.1"
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("org.ossiaustria.amigo.platform"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(
                ApiInfoBuilder()
                    .version(version)
                    .title("amigo-platform API")
                    .description("Almost RESTful API for amigo-platform auth, multimedia and messaging v$version")
                    .build()
            )
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        private const val AUTH_URLS = "/v1/auth/*"
        private const val AUTH_LOGIN_URLS = "/v1/auth/login"
        private const val AUTH_PASSWORD_URLS = "/v1/auth/password/*"
        private const val AUTH_PASSWORD_RESET_URLS = "/v1/auth/password/reset/*"
    }
}
