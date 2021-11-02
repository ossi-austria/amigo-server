package org.ossiaustria.amigo.platform.domain.models

import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.Id
import javax.persistence.JoinColumn

@Entity
data class LoginToken(

    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,

    @Column(length = 16, nullable = false)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = ForeignKey(name = "login_token_person_id_fkey"))
    val personId: UUID,

    val token: String,

    @CreatedDate
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @CreatedDate
    val expiresAt: ZonedDateTime? = null

)
