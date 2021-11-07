package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Album
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal interface AlbumRepository : CrudRepository<Album, UUID> {
    fun findAllByOwnerIdOrderByCreatedAt(ownerId: UUID): List<Album>
    fun findAllByOwnerIdIn(ownerIds: List<UUID>): List<Album>
    fun findByOwnerIdAndId(ownerId: UUID, albumId: UUID): Album?

}
