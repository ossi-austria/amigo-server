package org.ossiaustria.amigo.platform.domain.models

import org.ossiaustria.amigo.platform.domain.models.enums.CallState
import org.ossiaustria.amigo.platform.domain.models.enums.CallType
import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*

@Entity
data class Call(
    @Id
    @Column(length = 16, unique = true, nullable = false)
    override val id: UUID,

    @JoinColumn(name = "sender_id", nullable = false, foreignKey = ForeignKey(name = "call_person_sender_id_fkey"))
    override val senderId: UUID,

    @JoinColumn(name = "receiver_id", nullable = false, foreignKey = ForeignKey(name = "call_person_receiver_id_fkey"))
    override val receiverId: UUID,

    @Enumerated(EnumType.STRING)
    val callType: CallType,

    val startedAt: ZonedDateTime? = null,

    val finishedAt: ZonedDateTime? = null,

    @Enumerated(EnumType.STRING)
    val callState: CallState = CallState.CREATED,

    /**
     * Jitsi JWT token for sender
     */
    val senderToken: String? = null,

    /**
     * Jitsi JWT token for receiver
     */
    val receiverToken: String? = null,

    @CreatedDate
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sentAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null

) : Sendable<Call> {

    override fun withSentAt(time: ZonedDateTime) = this.copy(sentAt = time)

    override fun withRetrievedAt(time: ZonedDateTime) = this.copy(retrievedAt = time)

    fun cancel() = copy(
        callState = CallState.CANCELLED
    )

    fun deny() = copy(
        callState = CallState.DENIED,
        retrievedAt = this.retrievedAt ?: ZonedDateTime.now()
    )

    fun accept() = copy(
        callState = CallState.ACCEPTED,
        retrievedAt = this.retrievedAt ?: ZonedDateTime.now(),
        startedAt = ZonedDateTime.now(),
    )

    fun finish() = copy(
        callState = CallState.FINISHED,
        retrievedAt = this.retrievedAt ?: ZonedDateTime.now(),
        startedAt = this.startedAt ?: this.retrievedAt ?: ZonedDateTime.now().minusSeconds(1),
        finishedAt = ZonedDateTime.now(),
    )

    fun timeout() = copy(callState = CallState.TIMEOUT)

    fun tokenForPerson(personId: UUID): String? = when (personId) {
        senderId -> senderToken
        receiverId -> receiverToken
        else -> throw IllegalStateException("personId $personId is not sender ($senderId) or receiver ($receiverId)")
    }
}