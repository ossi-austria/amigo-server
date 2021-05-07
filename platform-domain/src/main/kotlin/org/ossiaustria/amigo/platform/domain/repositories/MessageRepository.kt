package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Message
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MessageRepository : CrudRepository<Message, UUID> {
    fun findAllBySenderIdOrderByCreatedAt(id: UUID): List<Message>
    fun findAllByReceiverIdOrderByCreatedAt(id: UUID): List<Message>
    fun findAllByReceiverIdAndSenderIdOrderByCreatedAtDesc(receiverId: UUID, senderId: UUID): List<Message>
}