package org.ossiaustria.amigo.platform.services

import org.ossiaustria.amigo.platform.domain.models.Account
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*


interface SecurityService {
    fun hasPersonId(account: Account, id: UUID?): Boolean
}

@Service
class SecurityServiceImpl() : SecurityService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun hasPersonId(account: Account, id: UUID?): Boolean {
        return account.persons.map { it.id }.contains(id)
    }
}