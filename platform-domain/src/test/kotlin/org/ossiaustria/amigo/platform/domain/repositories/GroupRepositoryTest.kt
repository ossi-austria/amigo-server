package org.ossiaustria.amigo.platform.domain.repositories

import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.testcommons.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID
import java.util.UUID.randomUUID


internal class GroupRepositoryTest : AbstractRepositoryTest<Group, GroupRepository>() {

    @Autowired
    override lateinit var repository: GroupRepository

    override fun initTest() {
        account = accounts.save(Account(randomUUID(), "email", "pass"))
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, Group> {
        val person = Person(randomUUID(), account.id, "Person/" + randomUUID(), id)
        val entity = Group(id = id, name = "Group/" + randomUUID()).add(person)
        return Pair(id, entity)
    }

    override fun changeEntity(entity: Group) = entity.copy(name = "changed")

    @Test
    fun `should save entity with all collections`() {
        val (id, entity) = createDefaultEntityPair()
        then(repository.findByIdOrNull(id)).isNull()
        repository.save(entity)
        val actual = repository.findByIdOrNull(id)
        then(actual).isNotNull
        then(actual?.members).isNotNull
        then(actual?.members?.size).isEqualTo(1)
    }

    @Test
    fun `should update entity with all collections`() {
        val (id, entity) = createDefaultEntityPair()
        then(repository.findByIdOrNull(id)).isNull()

        repository.save(
            entity
                .add(Person(randomUUID(), account.id, "name1"))
                .add(Person(randomUUID(), account.id, "name2"))
        )
        val actual = repository.findByIdOrNull(id)
        then(actual).isNotNull
        then(actual?.members).isNotNull
        then(actual?.members?.size).isEqualTo(3)
    }

    @Test
    fun `must not save duplicate name`() {
        violatesConstraints {
            repository.save(createDefaultEntity().copy(name = "slug1"))
            repository.save(createDefaultEntity().copy(name = "slug1"))
        }
    }

}
