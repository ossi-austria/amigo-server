package org.ossiaustria.amigo.platform.domain.services.multimedia

import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.models.StringValidator
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.repositories.AlbumRepository
import org.ossiaustria.amigo.platform.domain.repositories.AlbumShareRepository
import org.ossiaustria.amigo.platform.domain.repositories.GroupRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.SecurityError
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.UUID
import java.util.UUID.randomUUID

interface AlbumService {

    fun getOne(ownerId: UUID, albumId: UUID): Album?
    fun findWithOwner(ownerId: UUID): List<Album>
    fun findWithAccess(accessorId: UUID): List<Album>

    fun createAlbum(ownerId: UUID, name: String): Album

    fun count(): Long
}

@Service
class AlbumServiceImpl : AlbumService {

    @Autowired
    private lateinit var repository: AlbumRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var albumShareRepository: AlbumShareRepository

    @Autowired
    private lateinit var groupRepository: GroupRepository

    override fun createAlbum(ownerId: UUID, name: String): Album {
        StringValidator.validateNotBlank(name)
        return createNew(ownerId, name)
    }

    private fun createNew(ownerId: UUID, name: String) = repository.save(
        Album(
            id = randomUUID(),
            ownerId = ownerId,
            createdAt = ZonedDateTime.now(),
            name = name,
        )
    )

    override fun count(): Long = repository.count()

    override fun getOne(ownerId: UUID, albumId: UUID): Album? = repository.findByOwnerIdAndId(ownerId, albumId)

    override fun findWithOwner(ownerId: UUID) = repository.findAllByOwnerIdOrderByCreatedAt(ownerId).also {
        Log.info("findWithSender: ownerId=$ownerId -> ${it.size} results")
    }

    override fun findWithAccess(accessorId: UUID): List<Album> {
        val accessor = personRepository.findByIdOrNull(accessorId)
            ?: throw SecurityError.PersonNotFound("AccessorId not found")

        if (accessor.isDigitalUser())
            throw SecurityError.PersonNotFound("AccessorId is not an Analogue")

        val members = groupRepository.findByIdOrNull(accessor.groupId)?.members ?: emptyList()

        val ownerIds = members
            .filter { it.memberType != MembershipType.ANALOGUE }
            .filter { it.id != accessorId }
            .map { it.id }
        return repository.findAllByOwnerIdIn(ownerIds).toList().also {
            Log.info("findWithAccess: ${ownerIds.size} ownerIds -> ${it.size} results")
        }
    }

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }
}
