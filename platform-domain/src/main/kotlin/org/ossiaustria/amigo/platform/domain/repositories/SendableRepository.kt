package org.ossiaustria.amigo.platform.domain.repositories

import org.ossiaustria.amigo.platform.domain.models.Sendable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean
import java.util.*


@NoRepositoryBean
internal interface SendableRepository<T : Sendable<T>> : CrudRepository<T, UUID> {
    fun findAllBySenderIdOrderByCreatedAt(id: UUID): List<T>
    fun findAllByReceiverIdOrderByCreatedAt(id: UUID): List<T>
    fun findAllByReceiverIdAndSenderIdOrderByCreatedAtDesc(receiverId: UUID, senderId: UUID): List<T>
}

