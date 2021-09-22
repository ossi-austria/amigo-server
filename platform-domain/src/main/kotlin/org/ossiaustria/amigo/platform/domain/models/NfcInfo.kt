package org.ossiaustria.amigo.platform.domain.models

import org.ossiaustria.amigo.platform.domain.models.enums.NfcInfoType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.ForeignKey
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Table

@Entity
@Table(name = "nfc")
data class NfcInfo(

    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,

    @Column(length = 16, nullable = false)
    @JoinColumn(name = "creator_id", foreignKey = ForeignKey(name = "nfcs_person_creator_id_fk"))
    val creatorId: UUID,

    @Column(length = 16, nullable = false)
    @JoinColumn(name = "owner_id", foreignKey = ForeignKey(name = "nfcs_person_owner_id_fk"))
    val ownerId: UUID,

    @Enumerated(EnumType.STRING)
    val type: NfcInfoType,

    val name: String,

    @Column(length = 16, nullable = true)
    @JoinColumn(name = "linked_person_id", foreignKey = ForeignKey(name = "nfcs_person_linked_person_id_fk"))
    val linkedPersonId: UUID? = null,

    @Column(length = 16, nullable = true)
    @JoinColumn(name = "linked_album_id", foreignKey = ForeignKey(name = "nfcs_album_linked_album_id_fk"))
    val linkedAlbumId: UUID? = null,

    @CreatedDate
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    val updatedAt: ZonedDateTime? = null
)
