package org.ossiaustria.amigo.platform.domain.repositories


import org.ossiaustria.amigo.platform.domain.models.Group
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal interface GroupRepository : CrudRepository<Group, UUID> {
    fun findByName(name: String): List<Group>
    fun findByIdIn(ids: List<UUID>): List<Group>
}

