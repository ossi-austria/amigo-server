package org.ossiaustria.amigo.platform.domain.models

import java.time.ZonedDateTime
import java.util.*

interface Sendable {
    val id: UUID
    val createdAt: ZonedDateTime
    val sendAt: ZonedDateTime?
    val retrievedAt: ZonedDateTime?
    val senderId: UUID
    val receiverId: UUID
}