package org.ossiaustria.amigo.platform.domain.repositories

import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.UUID.randomUUID


internal class AccountRepositoryTest : AbstractRepositoryTest<Account, AccountRepository>() {

    @Autowired
    override lateinit var repository: AccountRepository


    override fun initTest() {
        group = groups.save(Group(randomUUID(), "group"))
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, Account> {
        val person = Person(randomUUID(), id, "owner", group.id)
        val entity = Account(id, "jpa-test@example.org", "password", listOf(person))
        return Pair(id, entity)
    }

    override fun changeEntity(entity: Account) = entity.copy(passwordEncrypted = "changed")

    @Test
    fun `save must not save new entity with duplicate email`() {
        violatesConstraints {
            repository.save(createDefaultEntity().copy(email = "email1@dot.com"))
            repository.save(createDefaultEntity().copy(email = "email1@dot.com"))
        }
    }

}
