package org.ossiaustria.amigo.platform.domain.models

import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import java.util.UUID
import java.util.UUID.randomUUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.ForeignKey
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Table
import javax.persistence.UniqueConstraint
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

    val avatarUrl: String? = null
) {

    override fun toString(): String {
        return "$id $accountId $name $memberType $groupId"
    }

    fun isAnAdmin() = memberType.isAtLeast(MembershipType.ADMIN)
    fun isAnalogue() = memberType == MembershipType.ANALOGUE
    fun isDigitalUser() = memberType != MembershipType.ANALOGUE
}
