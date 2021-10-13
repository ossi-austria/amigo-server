package org.ossiaustria.amigo.platform.domain.services.sendables

import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.NfcInfo
import org.ossiaustria.amigo.platform.domain.models.StringValidator
import org.ossiaustria.amigo.platform.domain.models.enums.NfcInfoType
import org.ossiaustria.amigo.platform.domain.repositories.AlbumRepository
import org.ossiaustria.amigo.platform.domain.repositories.NfcInfoRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.SecurityError
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.UUID
import java.util.UUID.randomUUID

interface NfcInfoService {

    fun createNfc(name: String, nfcRef: String, ownerId: UUID, creatorId: UUID): NfcInfo
    fun changeName(nfc: NfcInfo, newName: String): NfcInfo
    fun linkToAlbum(nfc: NfcInfo, albumId: UUID): NfcInfo
    fun linkToPerson(nfc: NfcInfo, personId: UUID): NfcInfo
    fun delete(nfc: NfcInfo)

    fun getOne(id: UUID): NfcInfo?
    fun findAllByCreator(creatorId: UUID): List<NfcInfo>
    fun findAllByOwner(ownerId: UUID): List<NfcInfo>
    fun findAllByPerson(personId: UUID): List<NfcInfo>
    fun findByLinkedAlbum(linkedAlbumId: UUID): List<NfcInfo>
    fun findByLinkedPerson(linkedPersonId: UUID): List<NfcInfo>
    fun count(): Long
    fun findAlbumsWithAccess(accessorId: UUID): List<Album>
    fun changeNfcInfo(existing: NfcInfo, name: String?, linkedPersonId: UUID?, linkedAlbumId: UUID?): NfcInfo
}

@Service
class NfcInfoServiceImpl : NfcInfoService {

    @Autowired
    private lateinit var repository: NfcInfoRepository

    @Autowired
    private lateinit var albumRepository: AlbumRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    override fun count(): Long = repository.count()

    override fun createNfc(name: String, nfcRef: String, ownerId: UUID, creatorId: UUID): NfcInfo {
        val creator = personRepository.findByIdOrNull(creatorId)
            ?: throw SecurityError.PersonNotFound(creatorId.toString())
        val owner = personRepository.findByIdOrNull(ownerId)
            ?: throw SecurityError.PersonNotFound(ownerId.toString())
        Group.assertSameGroup(creator.groupId, owner.groupId)
        val id = randomUUID()
        val nfc = NfcInfo(
            id,
            name = name,
            nfcRef = nfcRef,
            ownerId = ownerId,
            creatorId = creatorId,
            type = NfcInfoType.UNDEFINED
        )
        return repository.save(nfc)
    }

    override fun changeName(nfc: NfcInfo, newName: String): NfcInfo {
        StringValidator.validateNotBlank(newName)
        return repository.save(
            nfc.copy(
                updatedAt = ZonedDateTime.now(),
                name = newName
            )
        )
    }

    override fun linkToAlbum(nfc: NfcInfo, albumId: UUID): NfcInfo {
        val owner = personRepository.findByIdOrNull(nfc.ownerId)
            ?: throw SecurityError.PersonNotFound(nfc.ownerId.toString())
        val album = albumRepository.findByIdOrNull(albumId)
            ?: throw NotFoundException(ErrorCode.AlbumNotFound, albumId.toString())
        val albumOwner = personRepository.findByIdOrNull(album.ownerId)
            ?: throw SecurityError.PersonNotFound(albumId.toString())
        Group.assertSameGroup(owner.groupId, albumOwner.groupId)
        return repository.save(
            nfc.copy(
                updatedAt = ZonedDateTime.now(),
                linkedAlbumId = albumId,
                type = NfcInfoType.OPEN_ALBUM
            )
        )
    }

    override fun linkToPerson(nfc: NfcInfo, personId: UUID): NfcInfo {
        val owner = personRepository.findByIdOrNull(nfc.ownerId)
            ?: throw SecurityError.PersonNotFound(nfc.ownerId.toString())
        val linkedPerson = personRepository.findByIdOrNull(personId)
            ?: throw SecurityError.PersonNotFound(nfc.ownerId.toString())
        Group.assertSameGroup(owner.groupId, linkedPerson.groupId)
        return repository.save(
            nfc.copy(
                updatedAt = ZonedDateTime.now(),
                linkedPersonId = personId,
                type = NfcInfoType.CALL_PERSON
            )
        )
    }

    override fun changeNfcInfo(existing: NfcInfo, name: String?, linkedPersonId: UUID?, linkedAlbumId: UUID?): NfcInfo {
        val linkedOrExisting = if (linkedAlbumId != null) {
            linkToAlbum(existing, linkedAlbumId)
        } else if (linkedPersonId != null) {
            linkToPerson(existing, linkedPersonId)
        } else existing

        return if (name != null) {
            changeName(linkedOrExisting, name)
        } else linkedOrExisting

    }

    override fun findAlbumsWithAccess(accessorId: UUID): List<Album> = repository
        .findByOwnerId(accessorId)
        .mapNotNull { it.linkedAlbumId }
        .let { ids ->
            albumRepository.findAllById(ids).toList().also {
                Log.info("findWithAccess: ownerId=$accessorId -> ${it.size} results")
            }
        }

    override fun delete(nfc: NfcInfo) {
        return repository.delete(nfc)
    }

    override fun getOne(id: UUID): NfcInfo? = repository.findByIdOrNull(id)

    override fun findAllByCreator(creatorId: UUID) = repository.findByCreatorId(creatorId)
    override fun findAllByOwner(ownerId: UUID): List<NfcInfo> = repository.findByOwnerId(ownerId)

    override fun findAllByPerson(personId: UUID): List<NfcInfo> =
        repository.findByCreatorIdOrOwnerId(personId, personId)

    override fun findByLinkedAlbum(linkedAlbumId: UUID): List<NfcInfo> =
        repository.findByLinkedAlbumId(linkedAlbumId)


    override fun findByLinkedPerson(linkedPersonId: UUID): List<NfcInfo> =
        repository.findByLinkedPersonId(linkedPersonId)

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }
}
