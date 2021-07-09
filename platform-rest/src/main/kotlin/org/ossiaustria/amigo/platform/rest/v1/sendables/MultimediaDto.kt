package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import java.time.ZonedDateTime
import java.util.*


internal data class MultimediaDto(
    val id: UUID,
    val ownerId: UUID,
    val filename: String,
    val contentType: String? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val type: MultimediaType = MultimediaType.IMAGE,
    val size: Long? = 0,
    val albumId: UUID? = null,
)


internal fun Multimedia.toDto() = MultimediaDto(
    id = id,
    createdAt = createdAt,
    ownerId = ownerId,
    filename = filename,
    contentType = contentType,
    type = type,
    size = size,
    albumId = albumId,
)