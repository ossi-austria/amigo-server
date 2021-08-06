package org.ossiaustria.amigo.platform.rest.v1.multimedias

import io.micrometer.core.annotation.Timed
import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.domain.services.multimedia.AlbumService
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.springframework.web.bind.annotation.*
import java.util.*

@Timed
@RestController
@RequestMapping("/v1/albums", produces = ["application/json"], consumes = ["application/json"])
internal class AlbumsApi(private val albumService: AlbumService) {

    @PostMapping("")
    fun createAlbum(@RequestBody request: CreateAlbumRequest): AlbumDto {
        return albumService.createAlbum(request.ownerId, request.name).toDto()
    }

    @GetMapping("/own")
    fun own(tokenUserDetails: TokenUserDetails): List<AlbumDto> {
        val receiverId = tokenUserDetails.personsIds.first()
        return albumService.findWithOwner(receiverId).map(Album::toDto)
    }

    @GetMapping("/shared")
    fun shared(tokenUserDetails: TokenUserDetails): List<AlbumDto> {
        val accessorId = tokenUserDetails.personsIds.first()
        return albumService.findWithAccess(accessorId).map(Album::toDto)
    }

    @GetMapping("/{id}")
    fun getOne(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
    ): AlbumDto = getOneEntity(tokenUserDetails, id).toDto()

    private fun getOneEntity(
        tokenUserDetails: TokenUserDetails,
        id: UUID
    ): Album {
        val personId = tokenUserDetails.personsIds.first()
        val album = albumService.getOne(personId, id) ?: throw DefaultNotFoundException()
        return album
    }

    internal data class CreateAlbumRequest(
        val ownerId: UUID,
        val name: String,
    )
}
