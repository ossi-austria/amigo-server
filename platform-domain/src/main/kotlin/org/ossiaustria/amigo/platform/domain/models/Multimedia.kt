package org.ossiaustria.amigo.platform.domain.models

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

    override val senderId: UUID,
    override val receiverId: UUID,

    val ownerId: UUID,
//    val remoteUrl: String,

    @Enumerated(EnumType.STRING)
    val type: MultimediaType,
    val filename: String,
    val contentType: String? = null,
    val size: Long? = null,

//    val localUrl: String? = null,

//    @ManyToOne(fetch = FetchType.LAZY)
//    @Cascade(org.hibernate.annotations.CascadeType.DETACH)
//    @JoinColumn(
//        name = "album_id",
//        referencedColumnName = "id",
//        foreignKey = ForeignKey(name = "multimedias_album_id_id_fkey")
//    )
//    val album: Album? = null,

    @Column(name = "album_id")
    val albumId: UUID? = null,

    @CreatedDate
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val sentAt: ZonedDateTime? = null,
    override val retrievedAt: ZonedDateTime? = null,

    ) : Sendable<Multimedia> {

    override fun withSentAt(time: ZonedDateTime) = this.copy(sentAt = time)
    override fun withRetrievedAt(time: ZonedDateTime) = this.copy(retrievedAt = time)

    fun filename(): String {
        return "$id.${type}"
    }

    /**
     * In one case, a multimedia is created, but has no receiver yet, e.g. when creating a multimedia for a new album
     * We will just use owner/sender id for the receiver
     */
    fun isHidden() = this.senderId == this.receiverId

    companion object {
        const val URL_NONE = "NONE"
    }
}