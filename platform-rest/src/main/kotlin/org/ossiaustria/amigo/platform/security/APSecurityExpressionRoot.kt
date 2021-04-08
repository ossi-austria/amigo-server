package org.ossiaustria.amigo.platform.security

import org.ossiaustria.amigo.platform.repositories.GroupRepository
import org.ossiaustria.amigo.platform.repositories.PersonRepository
import org.ossiaustria.amigo.platform.services.auth.TokenDetails
import org.springframework.security.access.expression.SecurityExpressionRoot
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.core.Authentication
import java.util.*

class APSecurityExpressionRoot(
    authentication: Authentication,
    private val personRepository: PersonRepository,
    private val groupRepository: GroupRepository,
) : SecurityExpressionRoot(authentication), MethodSecurityExpressionOperations {

    private var returnObject: Any? = null
    private var filterObject: Any? = null
    private var target: Any? = null


//
//    fun postCanViewProject(): Boolean {
//        val id = getIdFromContext()
//        return (id != null && canViewProject(id))
//    }
//
//
//
//    fun postHasAccessToProject(minAccessLevel: String): Boolean {
//        val id = getIdFromContext()
//        return if (id != null) hasAccessToProject(id, minAccessLevel) else false
//    }
//
//    fun hasAccessToPipeline(pipelineId: UUID, minAccessLevel: String): Boolean {
//        val config = getPipelineConfigFromContext(pipelineId)
//        return if (config != null) hasAccessToProject(config.dataProjectId, minAccessLevel) else false
//    }
//
//    fun postHasAccessToPipeline(minAccessLevel: String): Boolean {
//        val id = getIdFromContext()
//        return if (id != null) hasAccessToPipeline(id, minAccessLevel) else false
//    }
//
//    fun canViewPipeline(id: UUID) = hasAccessToPipeline(id, AccessLevel.VISITOR.name)
//    fun postCanViewPipeline() = postHasAccessToPipeline(AccessLevel.VISITOR.name)
//
//
//
//
//
//    fun canViewProcessor(processorId: UUID): Boolean = hasAccessToProcessor(processorId, AccessLevel.VISITOR.name)
//    fun postCanViewProcessor() = postHasAccessToProcessor(AccessLevel.VISITOR.name)
//
//


    fun isUserItself(userId: UUID?): Boolean =
        if (userId != null) ((this.principal as? TokenDetails)?.accountId == userId) else false


    fun isUserItselfByToken(token: String?): Boolean {
        return if (token != null) {
            (this.principal as? TokenDetails)?.accessToken == token
        } else false
    }

//    fun userInGroup(): Boolean {
//        val id = getIdFromContext()
//        return if (id != null) userInGroup(id) else false
//    }

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
