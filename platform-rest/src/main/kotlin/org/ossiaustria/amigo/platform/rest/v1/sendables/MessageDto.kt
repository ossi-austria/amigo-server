package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Message
import java.time.ZonedDateTime
import java.util.*

internal interface SendableDto {
    val id: UUID
    val createdAt: ZonedDateTime
    val sendAt: ZonedDateTime?
    val retrievedAt: ZonedDateTime?
    val senderId: UUID
    val receiverId: UUID
}


internal data class MessageDto(
    override val id: UUID,
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sendAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null,
    override val senderId: UUID,
    override val receiverId: UUID,
    val text: String
) : SendableDto


internal fun Message.toDto() = MessageDto(
    id = id,
    createdAt = createdAt,
    sendAt = sendAt,
    retrievedAt = retrievedAt,
    senderId = senderId,
    receiverId = receiverId,
    text = text,
)