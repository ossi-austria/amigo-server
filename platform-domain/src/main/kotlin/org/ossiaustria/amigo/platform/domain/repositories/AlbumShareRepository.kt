package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.AlbumShare
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal interface AlbumShareRepository : CrudRepository<AlbumShare, UUID> {
    fun findAllByReceiverIdOrderByCreatedAt(id: UUID): List<AlbumShare>
}
