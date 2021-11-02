package org.ossiaustria.amigo.platform.domain.repositories

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.testcommons.Mocks
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import java.util.UUID.randomUUID


internal class AlbumRepositoryTest : AbstractRepositoryTest<Album, AlbumRepository>() {

    @Autowired
    override lateinit var repository: AlbumRepository

    override fun initTest() {
        initGroupAccountPerson()
    }

    override fun cleanTables() {
        repository.deleteAll()
        super.cleanTables()
    }

    override fun createDefaultEntityPair(id: UUID): Pair<UUID, Album> {
        val entity = Album(
            id = id,
            name = "Album/" + randomUUID(),
            ownerId = person.id,
            items = listOf(Mocks.multimedia(ownerId = person.id))
        )
        return Pair(id, entity)
    }

    override fun changeEntity(entity: Album) = entity.copy(name = "changed")

    @Test
    fun `save should save Multimedia list`() {
        val (id, entity) = createDefaultEntityPair()
        val saved = repository.save(entity)
        Assertions.assertThat(saved.items).isNotNull
        Assertions.assertThat(saved.items).isNotEmpty
    }

    @Test
    fun `must not save duplicate name for same owner`() {
        violatesConstraints {
            val entity = createDefaultEntity()
            val clone = createDefaultEntity().copy(ownerId = entity.ownerId, name = entity.name)
            repository.save(entity)
            repository.save(clone)
        }
    }

}
