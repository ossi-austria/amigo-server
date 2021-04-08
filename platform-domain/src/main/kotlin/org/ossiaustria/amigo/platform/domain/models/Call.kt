package org.ossiaustria.amigo.platform.domain.models

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

    @CreatedDate
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sendAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null,

    override val senderId: UUID,
    override val receiverId: UUID,

    @Enumerated(EnumType.STRING)
    val callType: CallType,

    val startedAt: ZonedDateTime? = null,
    val finishedAt: ZonedDateTime? = null,
) : Sendable