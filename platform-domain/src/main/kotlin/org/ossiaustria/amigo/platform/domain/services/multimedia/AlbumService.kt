package org.ossiaustria.amigo.platform.domain.services.multimedia

import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.models.StringValidator
import org.ossiaustria.amigo.platform.domain.repositories.AlbumRepository
import org.ossiaustria.amigo.platform.domain.repositories.AlbumShareRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*
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
    private lateinit var albumShareRepository: AlbumShareRepository


    override fun createAlbum(ownerId: UUID, name: String): Album {
        StringValidator.validateNotBlank(name)
        return createNew(ownerId, name)
    }

    private fun createNew(ownerId: UUID, name: String) = repository.save(Album(
        id = randomUUID(),
        ownerId = ownerId,
        createdAt = ZonedDateTime.now(),
        name = name,
    )
    )

    override fun count(): Long = repository.count()

    override fun getOne(ownerId: UUID, albumId: UUID): Album? = repository.findByOwnerIdAndId(ownerId, albumId)
//        ?: throw NotFoundException(ErrorCode.NotFound, "Album $albumId not found!")

    override fun findWithOwner(ownerId: UUID) = repository.findAllByOwnerIdOrderByCreatedAt(ownerId).also {
        Log.info("findWithSender: ownerId=$ownerId -> ${it.size} results")
    }

    override fun findWithAccess(accessorId: UUID): List<Album> = albumShareRepository
        .findAllByReceiverIdOrderByCreatedAt(accessorId)
        .map { it.albumId }
        .let { ids ->
            repository.findAllById(ids).toList().also {
                Log.info("findWithSender: ownerId=$accessorId -> ${it.size} results")
            }
        }

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }
}
