package org.ossiaustria.amigo.platform.domain.models

import java.time.ZonedDateTime
import java.util.UUID

interface Sendable<S> {
    val id: UUID
    val senderId: UUID
    val receiverId: UUID
    val createdAt: ZonedDateTime
    val sentAt: ZonedDateTime?
    val retrievedAt: ZonedDateTime?

    fun withSentAt(time: ZonedDateTime): S
    fun withRetrievedAt(time: ZonedDateTime): S

    fun isViewableBy(personId: UUID): Boolean {
        return (receiverId == personId || senderId == personId)
    }

    fun isRetrievableBy(personId: UUID): Boolean = (receiverId == personId)

    fun isSendableBy(personId: UUID): Boolean = (senderId == personId)

    fun isSent() = sentAt != null
    fun isRetrieved() = retrievedAt != null
}

