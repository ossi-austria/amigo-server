package org.ossiaustria.amigo.platform.rest.v1.multimedias

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.services.multimedia.AlbumService
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.ossiaustria.amigo.platform.rest.v1.common.Headers
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
@RequestMapping("/v1/albums", produces = ["application/json"])
internal class AlbumsApi(private val albumService: AlbumService) {

    @ApiOperation("Create a new Album as Owner")
    @PostMapping
    fun createAlbum(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,

        @RequestBody request: CreateAlbumRequest
    ): AlbumDto {
        val ownerId = account.person(personId).id
        return albumService.createAlbum(ownerId, request.name).toDto()
    }

    @ApiOperation("Get all Albums you can access")
    @GetMapping("/own")
    fun own(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): List<AlbumDto> {
        val receiverId = account.person(personId).id
        return albumService.findWithOwner(receiverId).map(Album::toDto)
    }

    @ApiOperation("Get all Albums you are allowed to view")
    @GetMapping("/shared")
    fun shared(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): List<AlbumDto> {
        val accessorId = account.person(personId).id
        return albumService.findWithAccess(accessorId).map(Album::toDto)
    }

    @ApiOperation("Get one Album")
    @GetMapping("/{id}")
    fun getOne(
        @PathVariable(value = "id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): AlbumDto {
        val album = albumService.getOne(account.person(personId).id, id) ?: throw DefaultNotFoundException()
        return album.toDto()
    }

    internal data class CreateAlbumRequest(
        val name: String,
    )
}
