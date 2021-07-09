package org.ossiaustria.amigo.platform.domain.models

import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import java.util.*
import java.util.UUID.randomUUID
import javax.persistence.*
import javax.validation.constraints.NotBlank


@Entity
@Table(
    name = "person", uniqueConstraints = [
        UniqueConstraint(name = "person_unique_name_group", columnNames = ["name", "groupId"])
    ]
)
data class Person(

    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,

    @JoinColumn(name = "account_id", foreignKey = ForeignKey(name = "person_account_account_id_fkey"))
    val accountId: UUID,

    @NotBlank
    val name: String,

    @JoinColumn(name = "group_id", foreignKey = ForeignKey(name = "persons_group_group_id_fkey"))
    val groupId: UUID = randomUUID(),

    @Enumerated(EnumType.STRING)
    val memberType: MembershipType = MembershipType.MEMBER,

    ) {

    override fun toString(): String {
        return "$id $accountId $name $memberType $groupId"
    }
}