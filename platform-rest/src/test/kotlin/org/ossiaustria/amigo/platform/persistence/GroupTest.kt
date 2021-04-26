package org.ossiaustria.amigo.platform.persistence

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.repositories.AccountRepository
import org.ossiaustria.amigo.platform.repositories.GroupRepository
import org.ossiaustria.amigo.platform.repositories.PersonRepository
import org.ossiaustria.amigo.platform.testcommons.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import java.util.UUID.randomUUID


class GroupTest : AbstractRepositoryTest() {

    @Autowired
    private lateinit var repository: GroupRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    private lateinit var account: Account

    private fun createEntity(
        name: String = "name",
    ): Pair<UUID, Group> {
        val groupId = randomUUID()
        val person = Person(randomUUID(), account.id, name, groupId)
        val entity = Group(id = groupId, name = name).add(person)
        return Pair(groupId, entity)
    }

    @BeforeEach
    fun prepare() {
        personRepository.deleteAll()
        accountRepository.deleteAll()
        repository.deleteAll()

        account = accountRepository.save(Account(randomUUID(), "email", "pass"))
    }


    @Test
    fun `should save entity with all collections`() {
        val (id, entity) = createEntity()
        then(repository.findByIdOrNull(id)).isNull()
        repository.save(entity)
        val actual = repository.findByIdOrNull(id)
        then(actual).isNotNull
        then(actual?.members).isNotNull
        then(actual?.members?.size).isEqualTo(1)
    }

    @Test
    fun `should update entity with all collections`() {
        val (id, entity) = createEntity()
        then(repository.findByIdOrNull(id)).isNull()

        repository.save(
            entity
                .add(Person(randomUUID(), account.id, "name1"))
                .add(Person(randomUUID(), account.id, "name1"))
        )
        val actual = repository.findByIdOrNull(id)
        then(actual).isNotNull
        then(actual?.members).isNotNull
        then(actual?.members?.size).isEqualTo(3)
    }

    @Test
    fun `must not save duplicate id`() {
        val (_, entity1) = createEntity("slug1")
        val (_, entity2) = createEntity("slug2")
        repository.save(entity1)
        commitAndFail {
            repository.save(entity2)
        }
    }

    @Test
    fun `must not save duplicate slug`() {
        commitAndFail {
            repository.save(createEntity("slug1").second)
            repository.save(createEntity("slug1").second)
        }
    }

    @Test
    fun `must not save duplicate username`() {
        commitAndFail {
            repository.save(createEntity("slug1").second)
            repository.save(createEntity("slug2").second)
        }
    }

    @Test
    fun `must not save duplicate email`() {
        commitAndFail {
            repository.save(createEntity("slug1").second)
            repository.save(createEntity("slug2").second)
        }
    }

    @Test
    fun `should refresh changes via update`() {
        val (_, entity) = createEntity()
        val saved = repository.save(entity)
        val newValue = "newname"
        val copy = saved.copy(name = newValue)
        val updated = repository.save(copy)
        then(updated).isNotNull
        then(updated.name).isEqualTo(newValue)
    }

    @Test
    fun `should delete the entity`() {
        val (_, entity) = createEntity()
        val saved = repository.save(entity)
        repository.delete(saved)
        then(saved).isNotNull
    }
}
