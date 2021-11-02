package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.enums.CallState
import org.ossiaustria.amigo.platform.domain.models.enums.CallType
import java.time.ZonedDateTime
import java.util.UUID


internal data class CallDto(
    override val id: UUID,
    override val senderId: UUID,
    override val receiverId: UUID,
    val callType: CallType,
    val startedAt: ZonedDateTime? = null,
    val finishedAt: ZonedDateTime? = null,
    val callState: CallState = CallState.CREATED,
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sentAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null
) : SendableDto

internal data class CallTokenDto(
    override val id: UUID,
    override val senderId: UUID,
    override val receiverId: UUID,
    val callType: CallType,
    val startedAt: ZonedDateTime? = null,
    val finishedAt: ZonedDateTime? = null,
    val callState: CallState = CallState.CREATED,
    val token: String? = null,
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sentAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null
) : SendableDto

internal fun Call.toDto() = CallDto(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    callType = callType,
    startedAt = startedAt,
    finishedAt = finishedAt,
    callState = callState,
    createdAt = createdAt,
    sentAt = sentAt,
    retrievedAt = retrievedAt,
)

//internal fun Call.toDto() = toTokenDto(null)

internal fun Call.toTokenDto(personId: UUID?) = CallTokenDto(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    callType = callType,
    startedAt = startedAt,
    finishedAt = finishedAt,
    callState = callState,
    token = personId?.let { this.tokenForPerson(personId) },
    createdAt = createdAt,
    sentAt = sentAt,
    retrievedAt = retrievedAt,
)
