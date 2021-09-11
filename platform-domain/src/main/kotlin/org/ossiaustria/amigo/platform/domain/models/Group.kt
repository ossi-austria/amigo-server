@file:Suppress("LeakingThis")

package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.services.ServiceError
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
    val members: List<Person> = mutableListOf()

) {

    fun analogue(): Person? =
        members.firstOrNull { it.memberType == MembershipType.ANALOGUE }

    fun owner(): Person =
        members.firstOrNull { it.memberType == MembershipType.OWNER }
            ?: throw ServiceError("Group without Owner", "Every group needs an Owner")

    fun admins(): List<Person> =
        members.filter { it.memberType == MembershipType.ADMIN }

    fun add(person: Person): Group =
        copy(members = this.members.toMutableList().also { it.add(person.copy(groupId = id)) })

    fun removeMember(member: Person): Group = copy(members = members.filter { it.id != member.id })

    fun findAdmin(account: Account) =
        members.firstOrNull { it.accountId == account.id && it.memberType.isAtLeast(MembershipType.ADMIN) }

    fun findMember(account: Account) = members.firstOrNull { it.accountId == account.id }
    fun findMember(personId: UUID) = members.firstOrNull { it.id == personId }
    fun findMember(person: Person) = findMember(person.id)

}