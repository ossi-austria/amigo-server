@file:Suppress("LeakingThis")

package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import java.util.*
import javax.persistence.*

@Entity
@Table(
    name = "groups", uniqueConstraints = [
        UniqueConstraint(name = "group_unique_name", columnNames = ["name"])
    ]
)
data class Group(

    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,
    val name: String,

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "groupId", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: MutableList<Person> = mutableListOf()

) {

    @Transient
    val analogue: Person? =
        members.firstOrNull { it.memberType == MembershipType.ANALOGUE }

    @Transient
    val owner: Person? =
        members.firstOrNull { it.memberType == MembershipType.OWNER }

    @Transient
    val admins: List<Person> =
        members.filter { it.memberType == MembershipType.ADMIN }

    fun add(person: Person): Group = apply {
        this.members.plusAssign(person.copy(groupId = this.id))
    }

    fun addAll(persons: List<Person>): Group = apply {
        persons.forEach { this.add(it) }
    }
}