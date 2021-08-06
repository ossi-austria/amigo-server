package org.ossiaustria.amigo.platform.domain.services.sendables

import org.ossiaustria.amigo.platform.domain.models.Sendable
import java.time.ZonedDateTime
import java.util.*


interface SendableService<S : Sendable<S>> {
    fun getOne(id: UUID): S?
    fun getAll(): List<S>
    fun findWithPersons(senderId: UUID?, receiverId: UUID?): List<S>
    fun findWithSender(senderId: UUID): List<S>
    fun findWithReceiver(receiverId: UUID): List<S>

    // operations for marking
    fun markAsRetrieved(id: UUID, time: ZonedDateTime): S
}
