package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal interface MultimediaRepository : CrudRepository<Multimedia, UUID> {
    fun findAllByOwnerIdOrderByCreatedAt(id: UUID): List<Multimedia>
    fun findAllByAlbumIdIn(ids: List<UUID>): List<Multimedia>
}
