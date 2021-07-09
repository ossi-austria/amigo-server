package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import java.time.ZonedDateTime
import java.util.*


internal data class MessageDto(
    override val id: UUID,
    override val senderId: UUID,
    override val receiverId: UUID,
    val text: String,
    val multimediaId: UUID? = null,
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sentAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null
) : SendableDto

internal data class MultiMessageDto(
    override val id: UUID,
    override val senderId: UUID,
    override val receiverId: UUID,
    val text: String,
    val multimedia: Multimedia? = null,
    val multimediaId: UUID? = multimedia?.id,
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sentAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null
) : SendableDto

internal fun Message.toDto() = MessageDto(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    text = text,
    multimediaId = multimediaId,
    createdAt = createdAt,
    sentAt = sentAt,
    retrievedAt = retrievedAt,
)

internal fun Message.toMultimediaDto() = MultiMessageDto(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    text = text,
    multimedia = multimedia,
    multimediaId = multimediaId,
    createdAt = createdAt,
    sentAt = sentAt,
    retrievedAt = retrievedAt,
)