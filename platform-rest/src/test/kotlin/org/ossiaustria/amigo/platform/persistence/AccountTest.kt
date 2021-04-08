package org.ossiaustria.amigo.platform.persistence

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Person
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import java.util.UUID.randomUUID
import javax.transaction.Transactional


class AccountTest : AbstractRepositoryTest() {

    @Autowired
    private lateinit var repository: AccountRepository

    private fun createEntity(
        slug: String = "slug",
        email: String = "email",
        changeAccountToken: String? = null
    ): Pair<UUID, Account> {
        val id = randomUUID()
        val person = Person(randomUUID(), slug, randomUUID())
        val entity = Account(
            id = id,
            passwordEncrypted = "enc",
            persons = listOf(person),
            email = email,
            lastLogin = null,
            changeAccountToken = changeAccountToken
        )
        return Pair(id, entity)
    }

    @BeforeEach
    fun prepare() {
        truncateDbTables(listOf("account", "account_token"), cascade = true)
    }

    @Transactional
    @Test
    fun `find works`() {
        val (id, entity) = createEntity()
        Assertions.assertThat(repository.findByIdOrNull(id)).isNull()
        repository.save(entity)
        Assertions.assertThat(repository.findByIdOrNull(id)).isNotNull
    }

    @Transactional
    @Test
    fun `save works`() {
        val (id, entity) = createEntity()
        Assertions.assertThat(repository.findByIdOrNull(id)).isNull()
        val saved = repository.save(entity)
        Assertions.assertThat(saved).isNotNull
        Assertions.assertThat(repository.findByIdOrNull(id)).isNotNull
    }

    @Transactional
    @Test
    fun `must not save duplicate id`() {
        val (_, entity1) = createEntity("slug1", "username1", "email1@dot.com")
        val (_, entity2) = createEntity("slug2", "username2", "email2@dot.com")
        repository.save(entity1)
        commitAndFail {
            repository.save(entity2)
        }
    }

    @Transactional
    @Test
    fun `must not save duplicate slug`() {
        commitAndFail {
            repository.save(createEntity("slug1", "username1", "email1@dot.com").second)
            repository.save(createEntity("slug1", "username2", "email2@dot.com").second)
        }
    }

    @Transactional
    @Test
    fun `must not save duplicate username`() {
        commitAndFail {
            repository.save(createEntity("slug1", "username1", "email1@dot.com").second)
            repository.save(createEntity("slug2", "username1", "email2@dot.com").second)
        }
    }

    @Transactional
    @Test
    fun `must not save duplicate email`() {
        commitAndFail {
            repository.save(createEntity("slug1", "username1", "email1@dot.com").second)
            repository.save(createEntity("slug2", "username2", "email1@dot.com").second)
        }
    }

    @Transactional
    @Test
    fun `update works`() {
        val (_, entity) = createEntity()
        val saved = repository.save(entity)
        val newValue = "newname"
        val copy = saved.copy(email = newValue)
        val updated = repository.save(copy)
        Assertions.assertThat(updated).isNotNull
        Assertions.assertThat(updated.email).isEqualTo(newValue)
    }

    @Transactional
    @Test
    fun `delete works`() {
        val (_, entity) = createEntity()
        val saved = repository.save(entity)
        repository.delete(saved)
        Assertions.assertThat(saved).isNotNull
    }
}
