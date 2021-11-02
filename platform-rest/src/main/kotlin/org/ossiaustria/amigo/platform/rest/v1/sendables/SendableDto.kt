package org.ossiaustria.amigo.platform.rest.v1.sendables

import java.time.ZonedDateTime
import java.util.UUID

internal interface SendableDto {
    val id: UUID
    val senderId: UUID
    val receiverId: UUID
    val createdAt: ZonedDateTime
    val sentAt: ZonedDateTime?
    val retrievedAt: ZonedDateTime?
}
