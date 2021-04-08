package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Cascade
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*

@Entity
data class Multimedia(

    @Id
    @Column(length = 16, unique = true, nullable = false)
    override val id: UUID,

    @CreatedDate
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sendAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null,

    override val senderId: UUID,
    override val receiverId: UUID,

    val ownerId: UUID,
    val remoteUrl: String,
    val localUrl: String,

    @Enumerated(EnumType.STRING)
    val type: MultimediaType,
    val size: Long? = null,


    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(org.hibernate.annotations.CascadeType.DETACH)
    @JoinColumn(
        name = "album_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "multimedias_album_id_id_fkey")
    )
    val album: Album?,

    @Column(name = "album_id", insertable = false, updatable = false)
    val albumId: UUID? = album?.id

) : Sendable