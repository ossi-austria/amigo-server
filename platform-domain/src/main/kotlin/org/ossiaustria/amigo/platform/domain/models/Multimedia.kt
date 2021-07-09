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
    val id: UUID,

    @Column(length = 16, nullable = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = ForeignKey(name = "multimedia_person_owner_id_fkey"))
    val ownerId: UUID,

    @Enumerated(EnumType.STRING)
    val type: MultimediaType,
    val filename: String,
    val contentType: String? = null,
    val size: Long? = null,

    @JoinColumn(name = "album_id", foreignKey = ForeignKey(name = "multimedias_album_id_id_fkey"))
    val albumId: UUID? = null,

    @CreatedDate
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    ) {

    fun filename(): String {
        return "$id.${type}"
    }

    fun isViewableBy(personId: UUID): Boolean {
        return this.ownerId == personId
    }

}