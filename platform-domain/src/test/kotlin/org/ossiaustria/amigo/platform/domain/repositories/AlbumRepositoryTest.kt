package org.ossiaustria.amigo.platform.domain.repositories

import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.UUID.randomUUID


internal class AlbumRepositoryTest : AbstractRepositoryTest<Album, AlbumRepository>() {

    @Autowired
    override lateinit var repository: AlbumRepository

    lateinit var owner: Person

    override fun initTest() {
        val group = groups.save(Group(randomUUID(), "group"))
        val account = accounts.save(Account(randomUUID(), "jpa-test@example.org", "password"))
        owner = persons.save(Person(randomUUID(), account.id, "owner", group.id))
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, Album> {
        val entity = Album(
            id = id,
            name = "Album/" + randomUUID(),
            owner = owner,
        )
        return Pair(id, entity)
    }

    override fun changeEntity(entity: Album) = entity.copy(name = "changed")


    @Test
    fun `must not save duplicate name for same owner`() {
        violatesConstraints {
            val entity = createDefaultEntity()
            val clone = createDefaultEntity().copy(owner = entity.owner, name = entity.name)
            repository.save(entity)
            repository.save(clone)
        }
    }

}
