package org.ossiaustria.amigo.platform.domain.repositories

import junit.framework.TestCase.assertNotNull
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import java.util.UUID.randomUUID


internal class AccountRepositoryTest : AbstractRepositoryTest<Account, AccountRepository>() {

    @Autowired
    override lateinit var repository: AccountRepository

    override fun initTest() {
        group = groups.save(Group(randomUUID(), "group"))
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, Account> {
        val person = Person(randomUUID(), id, "owner$id", group.id)
        val entity = Account(id, "jpa-test@example.org", "password", listOf(person))
        return Pair(id, entity)
    }

    override fun changeEntity(entity: Account) = entity.copy(passwordEncrypted = "changed")

    @Test
    fun `save must not save new entity with duplicate email`() {
        repository.save(createDefaultEntity().copy(email = "email1@dot.com"))
        violatesConstraints {
            repository.save(createDefaultEntity().copy(email = "email1@dot.com"))
        }
    }

    @Test
    fun `createdByAccountId must not reference an non-existing account`() {
        violatesConstraints {
            repository.save(createDefaultEntity().copy(createdByAccountId = randomUUID()))
        }
    }

    @Test
    fun `createdByAccountId should reference an existing account`() {
        val account = repository.save(createDefaultEntity().copy(email = "email1@dot.com"))
        val save = repository.save(
            createDefaultEntity().let {
                it.copy(
                    createdByAccountId = account.id,
                    persons = listOf(it.persons.first().copy(name = "second")),
                    email = "email2@dot.com"
                )
            }
        )
        assertNotNull(save)
    }

}
