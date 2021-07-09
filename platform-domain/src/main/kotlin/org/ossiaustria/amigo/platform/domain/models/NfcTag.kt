package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.ossiaustria.amigo.platform.domain.models.enums.NfcTagType
import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "nfc")
data class NfcTag(
    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @Fetch(value = FetchMode.JOIN)
    @JoinColumn(
        name = "creator_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "nfcs_person_creator_id_fk")
    )
    val creator: Person,

//    @Column(name = "creator_id", insertable = false, updatable = false)
//    val creatorId: UUID = creator.id,


    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @Fetch(value = FetchMode.JOIN)
    @JoinColumn(
        name = "owner_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "nfcs_person_owner_id_fk"),
    )
    val owner: Person,

//    @Column(name = "owner_id", insertable = false, updatable = false)
//    val ownerId: UUID = owner.id,
//
    @Enumerated(EnumType.STRING)
    val type: NfcTagType,

    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.JOIN)
    @JoinColumn(
        name = "linked_person_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "nfcs_person_linked_person_id_fk")
    )
    val linkedPerson: Person? = null,
    @Column(name = "linked_person_id", insertable = false, updatable = false)
    val linkedPersonId: UUID? = linkedPerson?.id,

    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.JOIN)
    @JoinColumn(
        name = "linked_multimedia_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "nfcs_multimedia_linked_multimedia_id_fk")
    )
    val linkedMedia: Multimedia? = null,
    @Column(name = "linked_multimedia_id", insertable = false, updatable = false)
    val linkedMediaId: UUID? = linkedMedia?.id,

    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.JOIN)
    @JoinColumn(
        name = "linked_album_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "nfcs_album_linked_album_id_fk")
    )
    val linkedAlbum: Album? = null,
    @Column(name = "linked_album_id", insertable = false, updatable = false)
    val linkedAlbumId: UUID? = linkedAlbum?.id,

    @CreatedDate
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    )