package org.ossiaustria.amigo.platform.rest.v1

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.models.NfcInfo
import org.ossiaustria.amigo.platform.domain.services.sendables.NfcInfoService
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.ossiaustria.amigo.platform.rest.v1.common.Headers
import org.ossiaustria.amigo.platform.rest.v1.multimedias.AlbumDto
import org.ossiaustria.amigo.platform.rest.v1.multimedias.toDto
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
@RequestMapping("/v1/nfcs", produces = ["application/json"], consumes = ["application/json"])
internal class NfcInfoApi(private val nfcInfoService: NfcInfoService) {

    @ApiOperation("Create a new NfcInfo as Creator for an Owner")
    @PostMapping("")
    fun createNfc(
        @ApiParam(required = true, value = "Create Request")
        @RequestBody
        request: CreateNfcInfoRequest
    ): NfcInfoDto {
        return nfcInfoService.createNfc(request.name, request.ownerId, request.creatorId).toDto()
    }

    @ApiOperation("Get all NfcInfo which have you as Owner")
    @GetMapping("/own")
    fun own(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): List<NfcInfoDto> {
        val receiverId = account.person(personId).id
        return nfcInfoService.findAllByOwner(receiverId).map(NfcInfo::toDto)
    }

    @ApiOperation("Get all NfcInfo which have you as Owner")
    @GetMapping("/created")
    fun created(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): List<NfcInfoDto> {
        val creatorId = account.person(personId).id
        return nfcInfoService.findAllByCreator(creatorId).map(NfcInfo::toDto)
    }

    @ApiOperation("Get all Albums which are accessible via your NfcInfos")
    @GetMapping("/albums")
    fun shared(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): List<AlbumDto> {
        val accessorId = account.person(personId).id
        return nfcInfoService.findAlbumsWithAccess(accessorId).map(Album::toDto)
    }

    @ApiOperation("Get one NfcInfo")
    @GetMapping("/{id}")
    fun getOne(
        @ApiParam(required = true, value = "UUID of NfcInfo")
        @PathVariable(value = "id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): NfcInfoDto {
        val album = nfcInfoService.getOne(id) ?: throw DefaultNotFoundException()
        return album.toDto()
    }

    internal data class CreateNfcInfoRequest(
        val name: String,
        val ownerId: UUID,
        val creatorId: UUID,
    )
}

