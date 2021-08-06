package org.ossiaustria.amigo.platform.rest.v1.multimedias

import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import java.time.ZonedDateTime
import java.util.*


internal data class AlbumDto(
    val id: UUID,
    val name: String,
    val ownerId: UUID,
    val items: List<MultimediaDto> = listOf(),
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    val updatedAt: ZonedDateTime? = null,
)


internal fun Album.toDto() = AlbumDto(
    id = id,
    ownerId = ownerId,
    name = name,
    items = items.map(Multimedia::toDto),
    createdAt = createdAt,
    updatedAt = updatedAt,
)