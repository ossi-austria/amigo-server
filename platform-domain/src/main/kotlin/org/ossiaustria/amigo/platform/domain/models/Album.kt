package org.ossiaustria.amigo.platform.domain.models

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ForeignKey
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.UniqueConstraint


@Entity
@Table(
    name = "album", uniqueConstraints = [
        UniqueConstraint(name = "album_unique_name_per_owner", columnNames = ["name", "ownerId"])
    ]
)
data class Album(
    @Id
    @Column(length = 16, unique = true, nullable = false)
    val id: UUID,

    val name: String,

    @JoinColumn(name = "owner_id", foreignKey = ForeignKey(name = "albums_person_owner_id_fkey"))
    val ownerId: UUID,

    @OneToMany(mappedBy = "albumId", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: List<Multimedia> = listOf(),

    @CreatedDate
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    val updatedAt: ZonedDateTime? = null,
)
