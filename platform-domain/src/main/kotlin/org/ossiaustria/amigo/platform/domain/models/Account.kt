package org.ossiaustria.amigo.platform.domain.models

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

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "account_id", foreignKey = ForeignKey(name = "person_account_account_id_fkey"))
    val persons: List<Person> = listOf(),

    val lastLogin: ZonedDateTime? = null,
    val lastRefresh: ZonedDateTime? = null,

    val lastRevocationDate: ZonedDateTime? = null,

    val changeAccountToken: String? = null,
    val changeAccountTokenCreatedAt: ZonedDateTime? = null,
) {

}