package org.ossiaustria.amigo.platform.config.security

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.services.auth.TokenResult
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.ParameterBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.schema.ModelRef
import springfox.documentation.service.Contact
import springfox.documentation.service.Parameter
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import java.util.UUID
import javax.servlet.http.HttpServletResponse


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
        webSecurity.ignoring().antMatchers(
            "/swagger-ui/**", "/configuration/**", "/swagger-ui.html",
            "/swagger-ui.html/", "/swagger-ui.html/*", "/swagger-ui.html/**",
            "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs", "/webjars/**"
        )
    }

    @Throws(Exception::class)
    public override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint()).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .anonymous().and()
            .authorizeRequests()
            .antMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-ui.html/",
                "/swagger-ui.html/*",
                "/webjars/swagger-ui/**"
            ).permitAll()
            .and().authorizeRequests()
            .antMatchers(
                "*",
                "/*",
                "/error",
                "/favicon.ico",
                "/actuator/**",
                PUBLIC_AVATAR_URLS,
                PUBLIC_MULTIMEDIA_URLS,
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
            .ignoredParameterTypes(
                Account::class.java,
                TokenResult::class.java,
                HttpServletResponse::class.java,
                AuthenticationPrincipal::class.java,
            )
            .select()
            .apis(RequestHandlerSelectors.basePackage("org.ossiaustria.amigo.platform.rest.v1"))
            .paths(PathSelectors.any())
            .build()
            .directModelSubstitute(UUID::class.java, String::class.java)
            .genericModelSubstitutes(ResponseEntity::class.java)
            .globalOperationParameters(operationParameters())
            .apiInfo(
                ApiInfoBuilder()
                    .version(version)
                    .license("MIT")
                    .contact(Contact("OSSI Austria", "ossi-austria.org", "hello@ossi-austria.org"))
                    .title("amigo-platform API")
                    .description("Almost RESTful API for amigo-platform auth, multimedia and messaging v$version")
                    .build()
            )
    }

    fun operationParameters(): List<Parameter>? {
        val headers: MutableList<Parameter> = ArrayList<Parameter>()
        headers.add(
            ParameterBuilder().name("Authorization")
                .description("Access Token DESC")
                .modelRef(ModelRef("string"))
                .parameterType("header")
                .required(true)
                .build()
        )
        headers.add(
            ParameterBuilder().name("Amigo-Person-Id")
                .description("Custom Head for current PersonId")
                .modelRef(ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build()
        )
        return headers
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        private const val AUTH_URLS = "/v1/auth/*"
        private const val PUBLIC_AVATAR_URLS = "/v1/persons/*/public/*"
        private const val PUBLIC_MULTIMEDIA_URLS = "/v1/multimedias/*/public/*"
        private const val AUTH_LOGIN_URLS = "/v1/auth/login"
        private const val AUTH_PASSWORD_URLS = "/v1/auth/password/*"
        private const val AUTH_PASSWORD_RESET_URLS = "/v1/auth/password/reset/*"
    }
}
