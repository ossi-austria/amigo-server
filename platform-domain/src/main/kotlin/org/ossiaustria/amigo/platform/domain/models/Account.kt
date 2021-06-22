package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(
    name = "account", uniqueConstraints = [
        UniqueConstraint(name = "account_unique_email", columnNames = ["email"])
    ]
)
data class Account(
    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,

    @NotBlank
    val email: String,

    @NotBlank
    val passwordEncrypted: String,

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "account_id", foreignKey = ForeignKey(name = "person_account_account_id_fkey"))
    val persons: List<Person> = listOf(),

    val lastLogin: ZonedDateTime? = null,
    val lastRefresh: ZonedDateTime? = null,

    val lastRevocationDate: ZonedDateTime? = null,

    val changeAccountToken: String? = null,
    val changeAccountTokenCreatedAt: ZonedDateTime? = null,

    val fcmToken: String? = null,
) {

    fun person() = persons.first()

    fun hasPersonId(personId: UUID?): Boolean = this.persons.map { it.id }.contains(personId)

}