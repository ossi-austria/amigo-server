package org.ossiaustria.amigo.platform.security

import org.ossiaustria.amigo.platform.services.auth.TokenDetails
import org.ossiaustria.amigo.platform.exceptions.AccessDeniedException

import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class TokenDetailsResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType.equals(TokenDetails::class.java)
    }

    override fun resolveArgument(parameter: MethodParameter,
                                 mavContainer: ModelAndViewContainer?,
                                 webRequest: NativeWebRequest,
                                 binderFactory: WebDataBinderFactory?): Any? {
        return SecurityContextHolder.getContext().authentication.principal as? TokenDetails
            ?: throw AccessDeniedException("Token details can not be resolved in current context")
    }
}
