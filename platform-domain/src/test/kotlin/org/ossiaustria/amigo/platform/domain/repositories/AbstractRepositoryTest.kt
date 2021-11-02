package org.ossiaustria.amigo.platform.domain.repositories

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID
import java.util.UUID.randomUUID

internal abstract class AbstractRepositoryTest<T, R : CrudRepository<T, UUID>> : AbstractWithJpaTest() {

    abstract val repository: R

    protected lateinit var account: Account
    protected lateinit var person: Person
    protected lateinit var group: Group

    fun createDefaultEntity(): T = createDefaultEntityPair(randomUUID()).second

    abstract fun createDefaultEntityPair(id: UUID = randomUUID()): Pair<UUID, T>
    abstract fun changeEntity(entity: T): T
    abstract fun initTest()

    @BeforeEach
    fun beforeEach() {
        cleanTables()
        initTest()
    }

    @Test
    fun `save should store an entity`() {
        val (id, entity) = createDefaultEntityPair()
        assertThat(repository.findByIdOrNull(id)).isNull()
        repository.save(entity)
        assertThat(repository.findByIdOrNull(id)).isNotNull
    }

    @Test
    fun `save should return a stored entity`() {
        val (id, entity) = createDefaultEntityPair()
        assertThat(repository.findByIdOrNull(id)).isNull()
        val saved = repository.save(entity)
        assertThat(saved).isNotNull
        assertThat(repository.findByIdOrNull(id)).isNotNull
    }

    @Test
    fun `update should store the changed entity`() {
        val (_, entity) = createDefaultEntityPair()
        val saved = repository.save(entity)
        val copy = changeEntity(saved)
        val updated = repository.save(copy)
        assertThat(updated).isNotNull
    }

    @Test
    fun `delete should remove an entity from database`() {
        val (_, entity) = createDefaultEntityPair()
        val saved = repository.save(entity)
        repository.delete(saved)
        assertThat(saved).isNotNull
    }

    protected fun initGroupAccountPerson() {
        account = accounts.save(Account(randomUUID(), "email@example.org", "pass"))
        group = groups.save(Group(randomUUID(), "group"))
        person = persons.save(Person(randomUUID(), account.id, "person", group.id))
    }
}
