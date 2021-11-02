package org.ossiaustria.amigo.platform.config.http

import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.ossiaustria.amigo.platform.security.AccountResolver
import org.ossiaustria.amigo.platform.security.TokenDetailsResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfigurer(
    val authService: AuthService,
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(TokenDetailsResolver())
        resolvers.add(AccountResolver(authService))
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        super.addResourceHandlers(registry)
        registry.addResourceHandler("/docs", "/docs/**", "/static/docs/**")
            .addResourceLocations("classpath:/static/docs/")
    }
}
