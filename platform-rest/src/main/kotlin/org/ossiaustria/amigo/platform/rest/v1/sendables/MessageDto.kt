package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Message
import java.time.ZonedDateTime
import java.util.*

internal interface SendableDto {
    val id: UUID
    val senderId: UUID
    val receiverId: UUID
    val createdAt: ZonedDateTime
    val sentAt: ZonedDateTime?
    val retrievedAt: ZonedDateTime?
}


internal data class MessageDto(
    override val id: UUID,
    override val senderId: UUID,
    override val receiverId: UUID,
    val text: String,
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sentAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null
) : SendableDto


internal fun Message.toDto() = MessageDto(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    text = text,
    createdAt = createdAt,
    sentAt = sentAt,
    retrievedAt = retrievedAt,
)