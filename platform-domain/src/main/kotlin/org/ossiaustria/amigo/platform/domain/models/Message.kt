package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Cascade
import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ForeignKey
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class Message(

    @Id
    @Column(length = 16, unique = true, nullable = false)
    override val id: UUID,

    @Column(length = 16, nullable = false)
    @JoinColumn(name = "sender_id", foreignKey = ForeignKey(name = "message_person_sender_id_fkey"), nullable = false)
    override val senderId: UUID,

    @Column(length = 16, nullable = false)
    @JoinColumn(
        name = "receiver_id",
        foreignKey = ForeignKey(name = "message_person_receiver_id_fkey"),
        nullable = false
    )
    override val receiverId: UUID,

    val text: String,

    /**
     * Message can reference one optional Multimedia
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.DETACH)
    @JoinColumn(
        name = "multimedia_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "message_multimedia_multimedia_id_fk")
    )
    val multimedia: Multimedia? = null,
    @Column(name = "multimedia_id", insertable = false, updatable = false)
    val multimediaId: UUID? = multimedia?.id,

    @CreatedDate
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sentAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null

) : Sendable<Message> {

    override fun withSentAt(time: ZonedDateTime) = this.copy(sentAt = time)
    override fun withRetrievedAt(time: ZonedDateTime) = this.copy(retrievedAt = time)
}
