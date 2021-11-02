package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.NfcInfo
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal interface NfcInfoRepository : CrudRepository<NfcInfo, UUID> {
    fun findByCreatorId(creatorId: UUID): List<NfcInfo>
    fun findByOwnerId(ownerId: UUID): List<NfcInfo>
    fun findByIdAndCreatorId(id: UUID, creatorId: UUID): NfcInfo?
    fun findByIdAndOwnerId(id: UUID, ownerId: UUID): NfcInfo?
    fun findByLinkedAlbumId(linkedAlbumId: UUID): List<NfcInfo>
    fun findByLinkedPersonId(linkedPersonId: UUID): List<NfcInfo>
}
