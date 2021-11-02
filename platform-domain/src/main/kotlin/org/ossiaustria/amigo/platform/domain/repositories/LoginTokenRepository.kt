package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.LoginToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal interface LoginTokenRepository : CrudRepository<LoginToken, UUID> {
    fun findByPersonId(id: UUID): LoginToken?
    fun findByToken(token: String): LoginToken?

}
