package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.models.Sendable
import org.ossiaustria.amigo.platform.domain.repositories.SendableRepository
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import java.time.ZonedDateTime
import java.util.*


interface SendableService<S : Sendable<S>> {
    fun getOne(id: UUID): S?
    fun getAll(): List<S>
    fun findWithPersons(receiverId: UUID?, senderId: UUID?): List<S>
    fun findWithSender(senderId: UUID): List<S>
    fun findWithReceiver(receiverId: UUID): List<S>

    // operations for marking
    fun markAsSent(id: UUID, time: ZonedDateTime = ZonedDateTime.now()): S
    fun markAsRetrieved(id: UUID, time: ZonedDateTime = ZonedDateTime.now()): S
}


internal class SendableServiceMixin<S : Sendable<S>>(
    private val repository: SendableRepository<S>
) : SendableService<S> {

    override fun findWithPersons(receiverId: UUID?, senderId: UUID?): List<S> {
        return when {
            (receiverId != null && senderId != null) ->
                repository.findAllByReceiverIdAndSenderIdOrderByCreatedAtDesc(receiverId, senderId)

            (receiverId != null) -> repository.findAllByReceiverIdOrderByCreatedAt(receiverId)

            (senderId != null) -> repository.findAllBySenderIdOrderByCreatedAt(senderId)

            else -> listOf()
        }.also {
            Log.info("findWithPersons: senderId=$senderId receiverId=$receiverId -> ${it.size} results")
        }
    }

    override fun getAll(): List<S> {
        return repository.findAll().toList().also {
            Log.info("getAll: -> ${it.size} results")
        }
    }

    override fun getOne(id: UUID): S? {
        return repository.findByIdOrNull(id)
    }

    override fun findWithSender(senderId: UUID): List<S> {
        return repository.findAllBySenderIdOrderByCreatedAt(senderId).also {
            Log.info("findWithSender: senderId=$senderId -> ${it.size} results")
        }
    }

    override fun findWithReceiver(receiverId: UUID): List<S> {
        return repository.findAllByReceiverIdOrderByCreatedAt(receiverId).also {
            Log.info("findWithReceiver: receiverId=$receiverId-> ${it.size} results")
        }
    }

    override fun markAsSent(id: UUID, time: ZonedDateTime): S {
        val existing = repository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.NotFound, "Sendable $id not found!")
        return repository.save(existing.withSentAt(time)).also {
            Log.info("markAsSent: ${it::class.java.simpleName} $id at $time")
        }
    }

    override fun markAsRetrieved(id: UUID, time: ZonedDateTime): S {
        val existing = repository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.NotFound, "Sendable $id not found!")
        return repository.save(existing.withRetrievedAt(time))
    }

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }


}