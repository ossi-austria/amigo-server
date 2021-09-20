package org.ossiaustria.amigo.platform.config.security


import org.ossiaustria.amigo.platform.security.APMethodSecurityExpressionHandler
import org.ossiaustria.amigo.platform.security.APPermissionEvaluator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration


@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class MethodSecurityConfig : GlobalMethodSecurityConfiguration() {
    override fun createExpressionHandler(): MethodSecurityExpressionHandler {
        val expressionHandler = APMethodSecurityExpressionHandler()
        expressionHandler.setPermissionEvaluator(APPermissionEvaluator())
        return expressionHandler
    }
}
