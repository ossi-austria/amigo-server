package org.ossiaustria.amigo.platform.domain.services.sendables

import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.Sendable
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.repositories.SendableRepository
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import java.time.ZonedDateTime
import java.util.*

internal class SendableServiceMixin<S : Sendable<S>>(
    private val repository: SendableRepository<S>,
    private val personRepository: PersonRepository
) : SendableService<S> {

    override fun findWithPersons(senderId: UUID?, receiverId: UUID?): List<S> {
        return when {
            (receiverId != null && senderId != null) ->
                repository.findAllByReceiverIdAndSenderIdOrderByCreatedAtDesc(receiverId, senderId)
            (receiverId != null) -> repository.findAllByReceiverIdOrderByCreatedAt(receiverId)
            (senderId != null) -> repository.findAllBySenderIdOrderByCreatedAt(senderId)
            else -> throw SendableError.PersonsNotProvided()
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

    override fun findWithPerson(personId: UUID): List<S> {
        return repository.findAllByReceiverIdOrSenderIdOrderByCreatedAtDesc(personId, personId)
            .also {
                Log.info("findWithPerson: (senderId OR receiverId) = $personId -> ${it.size} results")
            }
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

    override fun markAsRetrieved(id: UUID, time: ZonedDateTime): S {
        val sendable = getOne(id) ?: throw NotFoundException(ErrorCode.NotFound, "Sendable $id not found!")
        return repository.save(sendable.withRetrievedAt(time)).also {
            Log.info("markAsRetrieved: ${it::class.java.simpleName} $id at $time")
        }
    }

    fun validateSenderReceiver(senderId: UUID, receiverId: UUID): Pair<Person, Person> {
        if (senderId == receiverId) throw SendableError.PersonsAreTheSame()
        val sender = personRepository.findByIdOrNull(senderId) ?: throw DefaultNotFoundException()
        val receiver = personRepository.findByIdOrNull(receiverId) ?: throw DefaultNotFoundException()
        if (sender.groupId != receiver.groupId) throw SendableError.PersonsNotInSameGroup()
        return Pair(sender, receiver)
    }

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }

}