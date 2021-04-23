package org.ossiaustria.amigo.platform.security

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.exceptions.ForbiddenException
import org.ossiaustria.amigo.platform.repositories.AccountRepository
import org.ossiaustria.amigo.platform.services.auth.TokenUserDetails
import org.springframework.core.MethodParameter
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class AccountResolver(
    val accountRepository: AccountRepository
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType.equals(Account::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any {
        val tokenDetails = SecurityContextHolder.getContext().authentication.principal as? TokenUserDetails
            ?: throw ForbiddenException("Token details can not be resolved in current context")
        return accountRepository.findByIdOrNull(tokenDetails.accountId)
            ?: throw ForbiddenException("Token details can not be resolved in current context")

    }
}