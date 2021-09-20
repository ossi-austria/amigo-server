package org.ossiaustria.amigo.platform.rest.v1.multimedias

import io.micrometer.core.annotation.Timed
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.services.multimedia.AlbumService
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Timed
@RestController
@RequestMapping("/v1/albums", produces = ["application/json"], consumes = ["application/json"])
internal class AlbumsApi(private val albumService: AlbumService) {

    @PostMapping("")
    fun createAlbum(@RequestBody request: CreateAlbumRequest): AlbumDto {
        return albumService.createAlbum(request.ownerId, request.name).toDto()
    }

    @GetMapping("/own")
    fun own(
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ): List<AlbumDto> {
        val receiverId = account.person(personId).id
        return albumService.findWithOwner(receiverId).map(Album::toDto)
    }

    @GetMapping("/shared")
    fun shared(
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ): List<AlbumDto> {
        val accessorId = account.person(personId).id
        return albumService.findWithAccess(accessorId).map(Album::toDto)
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable(value = "id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ): AlbumDto {
        val album = albumService.getOne(account.person(personId).id, id) ?: throw DefaultNotFoundException()
        return album.toDto()
    }

    internal data class CreateAlbumRequest(
        val ownerId: UUID,
        val name: String,
    )
}
