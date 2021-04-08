package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Cascade
import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "album_share")
data class AlbumShare(
    @Id
    @Column(length = 16, unique = true, nullable = false)
    override val id: UUID,

    @CreatedDate
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sendAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null,

    override val senderId: UUID,
    override val receiverId: UUID,

    @OneToOne(fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.DETACH)
    @JoinColumn(
        name = "album_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "account_subject_person_id_fk")
    )
    val album: Album,

    @Column(name = "album_id", insertable = false, updatable = false)
    val albumId: UUID = album.id

) : Sendable