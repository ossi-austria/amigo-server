package org.ossiaustria.amigo.platform.domain.models

import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Message(
    @Id
    @Column(length = 16, unique = true, nullable = false)
    override val id: UUID,

    @CreatedDate
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sendAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null,

    override val senderId: UUID,
    override val receiverId: UUID,

    val text: String
) : Sendable<Message> {

    override fun withSentAt(time: ZonedDateTime) = this.copy(sendAt = time)

    override fun withRetrievedAt(time: ZonedDateTime) = this.copy(retrievedAt = time)
}