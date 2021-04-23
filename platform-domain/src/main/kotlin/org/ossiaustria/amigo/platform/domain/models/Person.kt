package org.ossiaustria.amigo.platform.domain.models

import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "person")
data class Person(

    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,

    @Column(name = "account_id")
    val accountId: UUID,

    val name: String,


//    @OneToOne(fetch = FetchType.EAGER)
//    @Fetch(value = FetchMode.JOIN)
//    @JoinColumn(
//        name = "group_id",
//        referencedColumnName = "id",
//        foreignKey = ForeignKey(name = "person_group_group_id_fk"))
//
//    val group: Group,

    @Column(name = "group_id")
    val groupId: UUID,

    @Enumerated(EnumType.STRING)
    val memberType: MembershipType = MembershipType.MEMBER,

    )