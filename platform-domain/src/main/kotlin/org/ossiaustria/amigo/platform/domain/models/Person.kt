package org.ossiaustria.amigo.platform.domain.models

import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import java.util.*
import java.util.UUID.randomUUID
import javax.persistence.*
import javax.validation.constraints.NotBlank


@Entity
@Table(name = "person")
data class Person(

    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,

    @Column(name = "account_id")
    val accountId: UUID,

    @NotBlank
    val name: String,

    @Column(name = "group_id")
    val groupId: UUID = randomUUID(),

    @Enumerated(EnumType.STRING)
    val memberType: MembershipType = MembershipType.MEMBER,

    ) {

    override fun toString(): String {
        return "$id $accountId $name $memberType $groupId"
    }
}