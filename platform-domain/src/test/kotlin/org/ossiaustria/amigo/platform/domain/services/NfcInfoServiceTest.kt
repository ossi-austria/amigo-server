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

        nfcInfoRepository.save(NfcInfo(existingId, personId1, personId2, NfcInfoType.OPEN_ALBUM, "text", "nfcRef"))
        nfcInfoRepository.save(NfcInfo(randomUUID(), personId2, personId1, NfcInfoType.OPEN_ALBUM, "text", "nfcRef"))
    }

    @Test
    fun `createNfc should create a new NFC with owner, creator and type=UNDEFINED`() {

        val result = service.createNfc("ndef", "ref", personId1, personId2)
        assertThat(result).isNotNull
        assertThat(result.ownerId).isEqualTo(personId1)
        assertThat(result.creatorId).isEqualTo(personId2)
        assertThat(result.name).isEqualTo("ndef")
        assertThat(result.nfcRef).isEqualTo("ref")
        assertThat(result.linkedAlbumId).isNull()
        assertThat(result.linkedPersonId).isNull()
        assertThat(result.updatedAt).isNull()
        assertThat(result.type).isEqualTo(NfcInfoType.UNDEFINED)
    }

    @Test
    fun `createNfc should persist the NfcInfo`() {
        val result = service.createNfc("ndef", "ref", personId1, personId2)
        val findByIdOrNull = nfcInfoRepository.findByIdOrNull(result.id)
        assertThat(findByIdOrNull).isNotNull
    }

    @Test
    fun `changeName should update existing NfcInfo with new name`() {
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        val result = service.changeName(nfc, "name")
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(nfc.id)
        assertThat(result.name).isEqualTo("name")
        assertThat(result.nfcRef).isEqualTo("ref")
        assertThat(result.updatedAt).isNotNull
    }

    @Test
    fun `changeName must not accept invalid name`() {
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        Assertions.assertThrows(ValidationException::class.java) {
            service.changeName(nfc, "")
        }
    }

    @Test
    fun `linkToAlbum should update existing NfcInfo with Album`() {
        val album = albumRepository.save(Album(randomUUID(), "name", personId2))
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        val result = service.linkToAlbum(nfc, album.id)
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(nfc.id)
        assertThat(result.linkedAlbumId).isEqualTo(album.id)
        assertThat(result.type).isEqualTo(NfcInfoType.OPEN_ALBUM)
    }

    @Test
    fun `linkToAlbum should update name to Album`() {
        val album = albumRepository.save(Album(randomUUID(), "name", personId2))
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        val result = service.linkToAlbum(nfc, album.id)
        assertThat(result).isNotNull
        assertThat(result.name).isEqualTo(album.name)
    }


    @Test
    fun `createNfc must not link unknown Persons`() {
        Assertions.assertThrows(SecurityError.PersonNotFound::class.java) {
            service.createNfc("ndef", "ref", personId1, randomUUID())
        }
    }

    @Test
    fun `createNfc must not link Persons from different Groups`() {
        Assertions.assertThrows(SecurityError.PersonsNotInSameGroup::class.java) {
            service.createNfc("ndef", "ref", personId1, personId3)
        }
    }

    @Test
    fun `linkToAlbum must not link Persons from different Groups`() {
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        val album = albumRepository.save(Album(randomUUID(), "name", personId3))
        Assertions.assertThrows(SecurityError.PersonsNotInSameGroup::class.java) {
            service.linkToAlbum(nfc, album.id)
        }
    }

    @Test
    fun `linkToAlbum-findWithAccess should give access to other Person's Album`() {
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        val findWithAccess = service.findAlbumsWithAccess(personId1)
        assertThat(findWithAccess).isEmpty()

        val album = albumRepository.save(Album(randomUUID(), "name", personId2))
        service.linkToAlbum(nfc, album.id)

        val after = service.findAlbumsWithAccess(personId1)
        assertThat(after).isNotEmpty
        assertThat(after.first().id).isEqualTo(album.id)
    }

    @Test
    fun `linkToPerson must not link Persons from different Groups`() {
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        Assertions.assertThrows(SecurityError.PersonsNotInSameGroup::class.java) {
            service.linkToPerson(nfc, person3.id)
        }
    }

    @Test
    fun `linkToPerson should update name to Person`() {
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        val result = service.linkToPerson(nfc, personId2)
        assertThat(result).isNotNull
        assertThat(result.name).isEqualTo(person2.name)
    }

    @Test
    fun `changeNfcInfo should update only name when provided`() {
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        val result = service.changeNfcInfo(nfc, "newname", null, null)
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(nfc.id)
        assertThat(result.name).isEqualTo("newname")
        assertThat(result.linkedAlbumId).isEqualTo(null)
        assertThat(result.type).isEqualTo(NfcInfoType.UNDEFINED)
    }

    @Test
    fun `changeNfcInfo should update albumId when provided`() {
        val album = albumRepository.save(Album(randomUUID(), "name", personId2))
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        val result = service.changeNfcInfo(nfc, "newname", null, album.id)
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(nfc.id)
        assertThat(result.name).isEqualTo("newname")
        assertThat(result.linkedAlbumId).isEqualTo(album.id)
        assertThat(result.linkedPersonId).isEqualTo(null)
        assertThat(result.type).isEqualTo(NfcInfoType.OPEN_ALBUM)
    }

    @Test
    fun `changeNfcInfo should update personId when provided`() {
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        val result = service.changeNfcInfo(nfc, "newname", personId2, null)
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(nfc.id)
        assertThat(result.name).isEqualTo("newname")
        assertThat(result.linkedAlbumId).isEqualTo(null)
        assertThat(result.linkedPersonId).isEqualTo(personId2)
        assertThat(result.type).isEqualTo(NfcInfoType.CALL_PERSON)
    }

    @Test
    fun `changeNfcInfo should link Album when too much is provided`() {
        val album = albumRepository.save(Album(randomUUID(), "name", personId2))
        val nfc = service.createNfc("ndef", "ref", personId1, personId2)
        val result = service.changeNfcInfo(nfc, "newname", personId2, album.id)
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(nfc.id)
        assertThat(result.name).isEqualTo("newname")
        assertThat(result.linkedAlbumId).isEqualTo(album.id)
        assertThat(result.linkedPersonId).isEqualTo(null)
        assertThat(result.type).isEqualTo(NfcInfoType.OPEN_ALBUM)
    }

    @Test
    fun `createNfc should call changeNfcInfo with linked Person`() {
        val result = service.createNfc("ndef", "ref", personId1, personId2, linkedPersonId = personId2)
        assertThat(result).isNotNull
        assertThat(result.name).isEqualTo(person2.name)
        assertThat(result.linkedAlbumId).isEqualTo(null)
        assertThat(result.linkedPersonId).isEqualTo(personId2)
        assertThat(result.type).isEqualTo(NfcInfoType.CALL_PERSON)
    }

    @Test
    fun `createNfc should call changeNfcInfo with linked Album `() {
        val album = albumRepository.save(Album(randomUUID(), "name", personId2))
        val result = service.createNfc("ndef", "ref", personId1, personId2, linkedAlbumId = album.id)
        assertThat(result).isNotNull
        assertThat(result.name).isEqualTo(album.name)
        assertThat(result.linkedAlbumId).isEqualTo(album.id)
        assertThat(result.linkedPersonId).isEqualTo(null)
        assertThat(result.type).isEqualTo(NfcInfoType.OPEN_ALBUM)
    }
}
