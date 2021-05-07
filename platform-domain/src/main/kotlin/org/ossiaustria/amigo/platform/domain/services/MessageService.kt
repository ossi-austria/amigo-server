package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.repositories.MessageRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

interface MessageService {
    fun getAll(): List<Message>
    fun findWithPersons(receiverId: UUID?, senderId: UUID?): List<Message>

}

@Service
internal class MessageServiceImpl(
    private val messageRepository: MessageRepository,
) : MessageService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun getAll(): List<Message> {
        return messageRepository.findAll().toList()
    }

    override fun findWithPersons(receiverId: UUID?, senderId: UUID?): List<Message> {
        return when {
            (receiverId != null && senderId != null) ->
                messageRepository.findAllByReceiverIdAndSenderIdOrderByCreatedAtDesc(receiverId, senderId)

            (receiverId != null) ->
                messageRepository.findAllByReceiverIdOrderByCreatedAt(receiverId)

            (senderId != null) ->
                messageRepository.findAllBySenderIdOrderByCreatedAt(senderId)

            else -> listOf()
        }
    }
}