package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.ossiaustria.amigo.platform.domain.services.SecurityError
import java.time.ZonedDateTime
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.UniqueConstraint
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

    // extra fields for AMIGOBOX users
    val createdByAccountId: UUID? = null,
    val hasValidMail: Boolean = true
) {

    fun person(personId: UUID?): Person = if (personId != null) {
        this.persons.find { it.id == personId } ?: throw SecurityError.PersonsNotInSameGroup()
    } else {
        primaryPerson()
    }

    fun primaryPerson() = persons.first()


    fun hasPersonId(personId: UUID?): Boolean = this.persons.map { it.id }.contains(personId)


}
