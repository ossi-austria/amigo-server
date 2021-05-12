package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Account
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface AccountRepository : CrudRepository<Account, UUID> {
    fun findOneByEmail(email: String): Account?
    fun findByChangeAccountToken(token: String): Account?
}