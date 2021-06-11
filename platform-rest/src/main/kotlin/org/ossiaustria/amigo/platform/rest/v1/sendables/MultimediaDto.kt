package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import java.time.ZonedDateTime
import java.util.*


internal data class MultimediaDto(
    override val id: UUID,
    override val senderId: UUID,
    override val receiverId: UUID,
    val ownerId: UUID,
    val remoteUrl: String,
    val localUrl: String? = null,

    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sentAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null,

    val type: MultimediaType = MultimediaType.IMAGE,
    val size: Long? = 0,
    val albumId: UUID? = null,

    ) : SendableDto


internal fun Multimedia.toDto() = MultimediaDto(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    createdAt = createdAt,
    sentAt = sentAt,
    retrievedAt = retrievedAt,
    ownerId = ownerId,
    remoteUrl = remoteUrl,
    localUrl = localUrl,
    type = type,
    size = size,
    albumId = albumId,
)