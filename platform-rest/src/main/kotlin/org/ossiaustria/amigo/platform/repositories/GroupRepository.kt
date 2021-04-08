package org.ossiaustria.amigo.platform.repositories


import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GroupRepository : CrudRepository<Group, UUID> {
    fun findByName(name: String): Group?
}