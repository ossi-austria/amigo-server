package org.ossiaustria.amigo.platform.security

import org.springframework.security.access.expression.SecurityExpressionRoot
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.core.Authentication

class APSecurityExpressionRoot(
    authentication: Authentication,
) : SecurityExpressionRoot(authentication), MethodSecurityExpressionOperations {

    private var returnObject: Any? = null
    private var filterObject: Any? = null
    private var target: Any? = null

    override fun getReturnObject() = returnObject

    override fun setReturnObject(returnObject: Any) {
        this.returnObject = returnObject
    }

    override fun getFilterObject() = filterObject

    override fun setFilterObject(filterObject: Any) {
        this.filterObject = filterObject
    }

    override fun getThis() = target

    fun setThis(target: Any) {
        this.target = target
    }

}
