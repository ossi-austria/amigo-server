package org.ossiaustria.amigo.platform.domain.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.models.NfcInfo
import org.ossiaustria.amigo.platform.domain.models.enums.NfcInfoType
import org.ossiaustria.amigo.platform.domain.repositories.AlbumRepository
import org.ossiaustria.amigo.platform.domain.repositories.NfcInfoRepository
import org.ossiaustria.amigo.platform.domain.services.sendables.NfcInfoService
import org.ossiaustria.amigo.platform.exceptions.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID.randomUUID

internal class NfcInfoServiceTest : AbstractServiceTest() {

    @Autowired
    lateinit var service: NfcInfoService

    @Autowired
    private lateinit var nfcInfoRepository: NfcInfoRepository

    @Autowired
    private lateinit var albumRepository: AlbumRepository

    @BeforeEach
    fun beforeEach() {
        nfcInfoRepository.deleteAll()
        cleanTables()

        mockPersons()

        nfcInfoRepository.save(NfcInfo(existingId, personId1, personId2, NfcInfoType.OPEN_ALBUM, "text"))
        nfcInfoRepository.save(NfcInfo(randomUUID(), personId2, personId1, NfcInfoType.OPEN_ALBUM, "text"))
    }

    @Test
    fun `createNfc should create a new NFC with owner, creator and type=UNDEFINED`() {

        val result = service.createNfc("ndef", personId1, personId2)
        assertThat(result).isNotNull
        assertThat(result.ownerId).isEqualTo(personId1)
        assertThat(result.creatorId).isEqualTo(personId2)
        assertThat(result.name).isEqualTo(result.id.toString())
        assertThat(result.linkedAlbumId).isNull()
        assertThat(result.linkedPersonId).isNull()
        assertThat(result.updatedAt).isNull()
        assertThat(result.type).isEqualTo(NfcInfoType.UNDEFINED)
    }

    @Test
    fun `createNfc should persist the NfcInfo`() {
        val result = service.createNfc("ndef", personId1, personId2)
        val findByIdOrNull = nfcInfoRepository.findByIdOrNull(result.id)
        assertThat(findByIdOrNull).isNotNull
    }

    @Test
    fun `changeName should update existing NfcInfo with new name`() {
        val nfc = service.createNfc("ndef", personId1, personId2)
        val result = service.changeName(nfc, "name")
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(nfc.id)
        assertThat(result.name).isEqualTo("name")
        assertThat(result.updatedAt).isNotNull
    }

    @Test
    fun `changeName must not accept invalid name`() {
        val nfc = service.createNfc("ndef", personId1, personId2)
        Assertions.assertThrows(ValidationException::class.java) {
            service.changeName(nfc, "")
        }
    }

    @Test
    fun `linkToAlbum should update existing NfcInfo with Album`() {
        val album = albumRepository.save(Album(randomUUID(), "name", personId2))
        val nfc = service.createNfc("ndef", personId1, personId2)
        val result = service.linkToAlbum(nfc, album)
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(nfc.id)
        assertThat(result.linkedAlbumId).isEqualTo(album.id)
        assertThat(result.type).isEqualTo(NfcInfoType.OPEN_ALBUM)
    }

    @Test
    fun `createNfc must not link unknown Persons`() {
        Assertions.assertThrows(SecurityError.PersonNotFound::class.java) {
            val nfc = service.createNfc("ndef", personId1, randomUUID())
        }
    }

    @Test
    fun `createNfc must not link Persons from different Groups`() {
        Assertions.assertThrows(SecurityError.PersonsNotInSameGroup::class.java) {
            val nfc = service.createNfc("ndef", personId1, personId3)
        }
    }

    @Test
    fun `linkToAlbum must not link Persons from different Groups`() {
        val nfc = service.createNfc("ndef", personId1, personId2)
        val album = albumRepository.save(Album(randomUUID(), "name", personId3))
        Assertions.assertThrows(SecurityError.PersonsNotInSameGroup::class.java) {
            val result = service.linkToAlbum(nfc, album)
        }
    }

    @Test
    fun `linkToAlbum-findWithAccess should give access to other Person's Album`() {
        val nfc = service.createNfc("ndef", personId1, personId2)
        val findWithAccess = service.findWithAccess(personId1)
        assertThat(findWithAccess).isEmpty()

        val album = albumRepository.save(Album(randomUUID(), "name", personId2))
        service.linkToAlbum(nfc, album)

        val after = service.findWithAccess(personId1)
        assertThat(after).isNotEmpty
        assertThat(after.first().id).isEqualTo(album.id)
    }

    @Test
    fun `linkToPerson must not link Persons from different Groups`() {
        val nfc = service.createNfc("ndef", personId1, personId2)
        Assertions.assertThrows(SecurityError.PersonsNotInSameGroup::class.java) {
            service.linkToPerson(nfc, person3)
        }
    }

}
