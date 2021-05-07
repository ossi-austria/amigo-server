package org.ossiaustria.amigo.platform.config.http

import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.security.AccountResolver
import org.ossiaustria.amigo.platform.security.TokenDetailsResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfigurer(
    val accountRepository: AccountRepository,
    val personRepository: PersonRepository
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(TokenDetailsResolver())
        resolvers.add(AccountResolver(accountRepository))
    }
}
