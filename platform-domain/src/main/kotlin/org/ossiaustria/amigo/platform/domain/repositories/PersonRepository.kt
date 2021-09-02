package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Person
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface PersonRepository : CrudRepository<Person, UUID> {
    fun findByName(name: String): Person?
    fun findByGroupId(id: UUID): List<Person>
    fun findByGroupIdAndId(groupId: UUID, id: UUID): Person?
    fun findByAccountId(id: UUID): List<Person>

}