package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Message
import org.springframework.stereotype.Repository
import java.util.*

@Repository
internal interface MessageRepository : SendableRepository<Message> {
    override fun findAllBySenderIdOrderByCreatedAt(id: UUID): List<Message>
    override fun findAllByReceiverIdOrderByCreatedAt(id: UUID): List<Message>
    override fun findAllByReceiverIdAndSenderIdOrderByCreatedAtDesc(receiverId: UUID, senderId: UUID): List<Message>
}

