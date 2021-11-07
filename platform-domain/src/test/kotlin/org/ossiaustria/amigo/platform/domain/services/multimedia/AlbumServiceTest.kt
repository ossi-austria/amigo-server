package org.ossiaustria.amigo.platform.domain.services.multimedia

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.repositories.AlbumRepository
import org.ossiaustria.amigo.platform.domain.services.AbstractServiceTest
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID.randomUUID

internal class AlbumServiceTest : AbstractServiceTest() {

    @Autowired
    lateinit var service: AlbumService

    @Autowired
    private lateinit var repository: AlbumRepository

    private lateinit var album_1_1: Album
    private lateinit var album_1_2: Album
    private lateinit var album_2_1: Album
    private lateinit var album_3_1: Album

    @BeforeEach
    fun beforeEach() {
        cleanTables()
        repository.deleteAll()

        mockPersons()

        album_1_1 = repository.save(Album(randomUUID(), "p1-1", person1.id))
        album_1_2 = repository.save(Album(randomUUID(), "p1-2", person1.id))
        album_2_1 = repository.save(Album(randomUUID(), "p2-1", person2.id))
        album_3_1 = repository.save(Album(randomUUID(), "p3-1", person3.id))
    }

    @Test
    fun `createAlbum should save a new Album`() {
        val result = service.createAlbum(personId1, "name")
        assertThat(result.ownerId).isEqualTo(personId1)
        assertThat(result.createdAt).isNotNull
    }

    @Test
    fun `getOne should return own Album`() {
        val result = service.getOne(person1.id, album_1_1.id)
        assertEquals(result?.id, album_1_1.id)
    }

    @Test
    fun `getOne should return not return another's Album`() {
//        assertThrows<NotFoundException> { service.getOne(person2.id, album_1_1.id) }
        assertNull(service.getOne(person2.id, album_1_1.id))
    }

    @Test
    fun `findWithOwner should return own Albums`() {
        val result = service.findWithOwner(person1.id).map { it.id }
        assertThat(result).containsAll(listOf(album_1_1.id, album_1_2.id))
    }

    @Test
    fun `findWithOwner should not return another's Albums`() {
        val result = service.findWithOwner(person2.id).map { it.id }
        assertThat(result).containsAll(listOf(album_2_1.id))
    }

    @Test
    fun `findWithAccess should return accessible Albums of same Gruop`() {
        val result = service.findWithAccess(person2.id).map { it.id }
        assertThat(result).containsAll(listOf(album_1_1.id, album_1_2.id))
    }

    @Test
    fun `findWithAccess should return accessible Albums based on receiverId`() {
        val result = service.findWithAccess(person2.id).map { it.id }
        assertThat(result).containsAll(listOf(album_1_1.id))
    }

    @Test
    fun `findWithAccess should return accessible Albums based on albumId`() {

        val result = service.findWithAccess(person2.id).map { it.id }
        assertThat(result).containsAll(listOf(album_1_1.id))
    }

    @Test
    fun `findWithAccess should not return own Albums`() {
        val result = service.findWithAccess(person2.id).map { it.ownerId }
        assertThat(result).doesNotContain(person2.id)
    }
}
