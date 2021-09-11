package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.ossiaustria.amigo.platform.domain.services.sendables.SendableError
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
    @Column(length = 16, unique = true, nullable = false)
    val email: String,

    @NotBlank
    val passwordEncrypted: String,

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "accountId", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val persons: List<Person> = listOf(),

    val lastLogin: ZonedDateTime? = null,
    val lastRefresh: ZonedDateTime? = null,

    val lastRevocationDate: ZonedDateTime? = null,

    val changeAccountToken: String? = null,
    val changeAccountTokenCreatedAt: ZonedDateTime? = null,

    val fcmToken: String? = null,
) {

    fun person(personId: UUID? = null): Person = if (personId != null) {
        this.persons.find { it.id == personId } ?: throw SendableError.PersonsNotInSameGroup()
    } else {
        persons.first()
    }

    fun hasPersonId(personId: UUID?): Boolean = this.persons.map { it.id }.contains(personId)

}