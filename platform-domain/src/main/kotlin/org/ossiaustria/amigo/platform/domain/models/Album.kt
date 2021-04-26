package org.ossiaustria.amigo.platform.domain.models

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "album")
data class Album(
    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,

    val name: String,

    @OneToOne(fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SELECT)
    @JoinColumn(
        name = "owner_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "albums_user_owner_id_fkey")
    )
    val owner: Person,

    @Column(name = "owner_id", insertable = false, updatable = false)
    val ownerId: UUID = owner.id,

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "album_id",
        foreignKey = ForeignKey(name = "multimedias_album_id_id_fkey")
    )
    val items: List<Multimedia>,

    @CreatedDate
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    val updatedAt: ZonedDateTime? = null,
)