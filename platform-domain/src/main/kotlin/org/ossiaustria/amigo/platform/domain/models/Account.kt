package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Cascade
import org.ossiaustria.amigo.platform.domain.models.Person
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "account")
data class Account(
    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,

    val email: String,

    val passwordEncrypted: String,

    @OneToMany(fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @JoinColumn(name = "account_id", foreignKey = ForeignKey(name = "person_account_account_id_fkey"))
    val persons: List<Person> = listOf(),

    val lastLogin: ZonedDateTime? = null,

    val jwtToken: String? = null,
    val jwtTokenCreatedAt: ZonedDateTime? = null,

    val changeAccountToken: String? = null,
    val changeAccountTokenCreatedAt: ZonedDateTime? = null,
) {

}