package org.ossiaustria.amigo.platform.rest.v1

import org.ossiaustria.amigo.platform.domain.models.NfcInfo
import org.ossiaustria.amigo.platform.domain.models.enums.NfcInfoType
import java.time.ZonedDateTime
import java.util.UUID

internal data class NfcInfoDto(
    val id: UUID,
    val creatorId: UUID,
    val ownerId: UUID,
    val type: NfcInfoType,
    val name: String,
    val nfcRef: String,//"04266F62C06780"
    val linkedPersonId: UUID? = null,
    val linkedAlbumId: UUID? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime? = null
)

internal fun NfcInfo.toDto() = NfcInfoDto(
    id = id,
    creatorId = creatorId,
    ownerId = ownerId,
    type = type,
    name = name,
    nfcRef = nfcRef,
    linkedPersonId = linkedPersonId,
    linkedAlbumId = linkedAlbumId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
