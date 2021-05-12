package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.repositories.MessageRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*
import java.util.UUID.randomUUID

interface MessageService : SendableService<Message> {
    fun createMessage(senderId: UUID, receiverId: UUID, text: String): Message
}

@Service
class MessageServiceImpl : MessageService {

    @Autowired
    private lateinit var repository: MessageRepository

    private val wrapper = SendableServiceMixin(repository)

    override fun createMessage(senderId: UUID, receiverId: UUID, text: String): Message {
        val message = Message(
            id = randomUUID(),
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            createdAt = ZonedDateTime.now(),
            retrievedAt = null,
            sendAt = null,
        )
        return repository.save(message).also {
            Log.info("createMessage: senderId=$senderId receiverId=$receiverId -> $text")
        }
    }

    override fun getOne(id: UUID): Message? = wrapper.getOne(id)

    override fun getAll(): List<Message> = wrapper.getAll()

    override fun findWithPersons(receiverId: UUID?, senderId: UUID?) =
        wrapper.findWithPersons(receiverId, senderId)

    override fun findWithSender(senderId: UUID) = wrapper.findWithSender(senderId)

    override fun findWithReceiver(receiverId: UUID) = wrapper.findWithReceiver(receiverId)

    override fun markAsSent(id: UUID, time: ZonedDateTime) = wrapper.markAsSent(id, time)

    override fun markAsRetrieved(id: UUID, time: ZonedDateTime) = wrapper.markAsRetrieved(id, time)

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }
}