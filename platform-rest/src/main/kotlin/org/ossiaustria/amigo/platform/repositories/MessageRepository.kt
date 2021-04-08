package org.ossiaustria.amigo.platform.repositories

import org.ossiaustria.amigo.platform.domain.models.Message
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MessageRepository : CrudRepository<Message, UUID> {
    fun findAllBySenderIdOrderByCreatedAt(id: UUID): List<Message>
    fun findAllByReceiverIdOrderByCreatedAt(id: UUID): List<Message>
    fun findAllByReceiverIdAndSenderIdOrderByCreatedAtDesc(receiverId: UUID, senderId: UUID): List<Message>
}

//@Repository
//internal interface MessageRepositoryImpl : MessageRepository,CrudRepository<Message, UUID> {
//    override fun findAllBySenderIdOrderByCreatedAt(id: UUID): List<Message>
//    override fun findAllByReceiverIdOrderByCreatedAt(id: UUID): List<Message>
//    override fun findAllByReceiverIdAndSenderIdOrderByCreatedAtDesc(receiverId: UUID, senderId:UUID): List<Message>
//}