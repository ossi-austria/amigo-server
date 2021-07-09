package org.ossiaustria.amigo.platform.domain.repositories

import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Person
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


internal class PersonRepositoryTest : AbstractRepositoryTest<Person, PersonRepository>() {

    @Autowired
    override lateinit var repository: PersonRepository

    override fun initTest() {
        initGroupAccountPerson()
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, Person> {
        val entity = Person(id, account.id, "owner", group.id)
        return Pair(id, entity)
    }

    override fun changeEntity(entity: Person) = entity.copy(name = "changed")

    @Test
    fun `save must not save new entity with duplicate name in same group`() {
        violatesConstraints {
            repository.save(createDefaultEntity().copy(name = "email1@dot.com"))
            repository.save(createDefaultEntity().copy(name = "email1@dot.com"))
        }
    }

}
