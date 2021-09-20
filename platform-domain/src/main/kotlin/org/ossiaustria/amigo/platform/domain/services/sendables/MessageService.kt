package org.ossiaustria.amigo.platform.domain.services.sendables

import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.StringValidator
import org.ossiaustria.amigo.platform.domain.repositories.MessageRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.messaging.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*
import java.util.UUID.randomUUID

interface MessageService : SendableService<Message> {
    fun createMessage(senderId: UUID, receiverId: UUID, text: String, multimedia: Multimedia?): Message
    fun count(): Long
}

@Service
class MessageServiceImpl : MessageService {

    @Autowired
    private lateinit var repository: MessageRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var notificationService: NotificationService

    private val wrapper: SendableServiceMixin<Message> by lazy { SendableServiceMixin(repository, personRepository) }

    override fun createMessage(senderId: UUID, receiverId: UUID, text: String, multimedia: Multimedia?): Message {
        StringValidator.validateNotBlank(text)
        wrapper.validateSenderReceiver(senderId, receiverId)
        val message = Message(
            id = randomUUID(),
            senderId = senderId,
            receiverId = receiverId,
            multimediaId = multimedia?.id,
            text = text,
            createdAt = ZonedDateTime.now(),
            retrievedAt = null,
            sentAt = null,
        )

        Log.info("createMessage: senderId=$senderId receiverId=$receiverId -> $text, multimediaId: ${multimedia?.id}")

        val success = notificationService.messageSent(receiverId, message)
        return if (success) {
            repository.save(message.copy(sentAt = ZonedDateTime.now())).also {
                Log.info("sent Message: senderId=$senderId receiverId=$receiverId -> $text")
            }
        } else {
            repository.save(message).also {
                Log.warn("could not send Message: senderId=$senderId receiverId=$receiverId -> $text")
            }
        }

    }

    override fun count(): Long = repository.count()

    override fun getOne(id: UUID): Message? = wrapper.getOne(id)

    override fun getAll(): List<Message> = wrapper.getAll()

    override fun findWithPersons(senderId: UUID?, receiverId: UUID?) =
        wrapper.findWithPersons(senderId, receiverId)

    override fun findWithPerson(personId: UUID) = wrapper.findWithPerson(personId)

    override fun findWithSender(senderId: UUID) = wrapper.findWithSender(senderId)

    override fun findWithReceiver(receiverId: UUID) = wrapper.findWithReceiver(receiverId)

    override fun markAsRetrieved(id: UUID, time: ZonedDateTime) = wrapper.markAsRetrieved(id, time)

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }
}