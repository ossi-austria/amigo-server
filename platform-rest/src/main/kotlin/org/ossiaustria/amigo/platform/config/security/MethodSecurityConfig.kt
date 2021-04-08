package org.ossiaustria.amigo.platform.config.security


import org.ossiaustria.amigo.platform.repositories.GroupRepository
import org.ossiaustria.amigo.platform.repositories.PersonRepository
import org.ossiaustria.amigo.platform.security.APMethodSecurityExpressionHandler
import org.ossiaustria.amigo.platform.security.APPermissionEvaluator
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration


@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class MethodSecurityConfig(
    private val personRepository: PersonRepository,
    private val groupRepository: GroupRepository,
) : GlobalMethodSecurityConfiguration() {
    override fun createExpressionHandler(): MethodSecurityExpressionHandler {
        val expressionHandler = APMethodSecurityExpressionHandler(
            personRepository,
            groupRepository,
        )
        expressionHandler.setPermissionEvaluator(APPermissionEvaluator())
        return expressionHandler
    }
}
