package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Album
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface AlbumRepository : CrudRepository<Album, UUID> {
    fun findAllByOwnerIdOrderByCreatedAt(ownerId: UUID): List<Album>
    fun findByOwnerIdAndId(ownerId: UUID, albumId: UUID): Album?
    fun findAllByOwnerIdAndNameLikeOrderByCreatedAt(id: UUID, name: String): List<Album>

}