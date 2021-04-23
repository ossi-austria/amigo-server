package org.ossiaustria.amigo.platform.domain.models

import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "groups")
data class Group(

    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,
    val name: String,

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "group_id",
        foreignKey = ForeignKey(name = "persons_group_group_id_fkey")
    )
    val members: List<Person> = listOf(),
) {
    @Transient
    val centerPerson: Person? =
        members.firstOrNull { it.memberType == MembershipType.CENTER }

    @Transient
    val admins: List<Person> =
        members.filter { it.memberType == MembershipType.ADMIN }
}