package org.ossiaustria.amigo.platform.rest.v1

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
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/nfcs", produces = ["application/json"])
internal class NfcInfoApi(
    private val nfcInfoService: NfcInfoService,
) {

    @ApiOperation("Create a new NfcInfo as Creator for an Owner")
    @PostMapping("")
    fun createNfc(
        @ApiParam(required = true, value = "Create Request")
        @RequestBody
        request: CreateNfcInfoRequest
    ): NfcInfoDto {
        return nfcInfoService.createNfc(
            request.name,
            request.nfcRef,
            request.ownerId,
            request.creatorId,
            request.linkedPersonId,
            request.linkedAlbumId
        ).toDto()
    }


    @ApiOperation("Change name or linked Entity of an NFC you created")
    @PatchMapping("/{id}")
    fun changeNfc(
        @ApiParam(required = true, value = "UUID of NfcInfo")
        @PathVariable(value = "id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,

        @ApiParam(required = true, value = "Create Request")
        @RequestBody
        request: ChangeNfcInfoRequest
    ): NfcInfoDto {
        val creatorId = account.person(personId).id
        val existing = nfcInfoService.findAllByCreator(creatorId).find {
            it.id == id
        } ?: throw DefaultNotFoundException()

        return nfcInfoService.changeNfcInfo(
            existing, request.name,
            request.linkedPersonId,
            request.linkedAlbumId
        ).toDto()
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

    @ApiOperation("Delete created NfcInfo")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOne(
        @ApiParam(required = true, value = "UUID of NfcInfo")
        @PathVariable(value = "id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) {
        val accessorId = account.person(personId).id
        nfcInfoService.delete(id, accessorId)
    }

    internal data class CreateNfcInfoRequest(
        val name: String,
        val nfcRef: String,
        val ownerId: UUID,
        val creatorId: UUID,
        val linkedAlbumId: UUID? = null,
        val linkedPersonId: UUID? = null,
    )

    internal data class ChangeNfcInfoRequest(
        val name: String? = null,
        val linkedAlbumId: UUID? = null,
        val linkedPersonId: UUID? = null,
    )
}

