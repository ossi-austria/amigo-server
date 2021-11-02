package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Cascade
import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ForeignKey
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "album_share")
data class AlbumShare(
    @Id
    @Column(length = 16, unique = true, nullable = false)
    override val id: UUID,

    @Column(length = 16, nullable = false)
    override val senderId: UUID,

    @Column(length = 16, nullable = false)
    override val receiverId: UUID,

    @OneToOne(fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.DETACH)
    @JoinColumn(
        name = "album_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "album_share_album_album_id_fk")
    )
    val album: Album,

    @Column(name = "album_id", insertable = false, updatable = false)
    val albumId: UUID = album.id,

    @CreatedDate
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sentAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null

) : Sendable<AlbumShare> {

    override fun withSentAt(time: ZonedDateTime) = this.copy(sentAt = time)

    override fun withRetrievedAt(time: ZonedDateTime) = this.copy(retrievedAt = time)
}
