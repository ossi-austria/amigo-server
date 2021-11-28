package org.ossiaustria.amigo.platform.rest.v1.multimedias

import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.services.multimedia.MultimediaService
import org.ossiaustria.amigo.platform.exceptions.BadRequestException
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.ossiaustria.amigo.platform.rest.v1.common.Headers
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Timed(value = "amigo.api.multimedias")
@RestController
@RequestMapping("/v1/multimedias", produces = ["application/json"])
internal class MultimediasApi(private val multimediaService: MultimediaService) {

    @ApiOperation("Upload a File and create new Multimedia")
    @PostMapping(
        consumes = [
            MediaType.IMAGE_GIF_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun createMultimedia(
        @RequestParam(value = "ownerId")
        ownerId: UUID,

        @RequestParam(value = "albumId", required = false)
        albumId: UUID? = null,

        @RequestPart(value = "name", required = false)
        name: String?,

        @RequestPart("file")
        file: MultipartFile,
    ): MultimediaDto {
        return multimediaService.createMultimedia(ownerId, albumId, name, file).toDto()
    }

    @ApiOperation("Upload a File to update Multimedia")
    @PostMapping(
        "/{id}/file",
        consumes = [
            MediaType.IMAGE_GIF_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadFile(
        @PathVariable(value = "id") id: UUID,
        @RequestPart("file")
        file: MultipartFile,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true) account: Account,
    ): Multimedia {
        val multimedia = getOneEntity(account.person(personId).id, id)
        return multimediaService.uploadFile(multimedia, file)
    }

    @ApiOperation("Download File of Multimedia")
    @GetMapping("/{id}/file")
    fun downloadFile(
        @PathVariable(value = "id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): ResponseEntity<Resource> {
        val multimedia = getOneEntity(account.person(personId).id, id)
        try {
            return multimediaResource(multimedia)
        } catch (e: Exception) {
            throw BadRequestException(ErrorCode.NotFound, e.message!!)
        }
    }

    @ApiOperation("Download Public File of Multimedia")
    @GetMapping("/{id}/public/{filename}")
    fun publicDownloadFile(
        @PathVariable(value = "id")
        id: UUID,

        @PathVariable(value = "filename")
        filename: String,
    ): ResponseEntity<Resource> {

        val multimediaEntity = multimediaService.getOne(id)
            ?: throw NotFoundException(ErrorCode.NotFound, "This Person has no avatar (yet).")

        val multimedia = getOneEntity(multimediaEntity.ownerId, id)

        // use provided filename as Security check
        if (multimedia.filename != filename)
            throw NotFoundException(ErrorCode.NotFound, "Multimedia $id not found!")

        try {
            return multimediaResource(multimedia)
        } catch (e: Exception) {
            throw BadRequestException(ErrorCode.NotFound, e.message!!)
        }
    }

    @ApiOperation("Get all own Multimedias")
    @GetMapping("/own")
    fun own(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): List<MultimediaDto> {
        val receiverId = account.person(personId).id
        return multimediaService.findWithOwner(receiverId).map(Multimedia::toDto)
    }

    @ApiOperation("Get all Multimedias shared for you")
    @GetMapping("/shared")
    fun shared(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): List<MultimediaDto> {
        val accessorId = account.person(personId).id
        return multimediaService.findWithAccess(accessorId).map(Multimedia::toDto)
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable(value = "id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): MultimediaDto = getOneEntity(account.person(personId).id, id).toDto()

    private fun multimediaResource(multimedia: Multimedia): ResponseEntity<Resource> {
        val resource = multimediaService.loadFile(multimedia)
        val filename = multimedia.filename()
        val header = "attachment; filename=$filename"
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, header)
            .header(HttpHeaders.CONTENT_TYPE, multimedia.contentType)
            .header(HttpHeaders.CONTENT_LENGTH, multimedia.size?.toString() ?: "0")
            .body(resource)
    }

    private fun getOneEntity(
        personId: UUID,
        id: UUID
    ): Multimedia {
        val multimedia = multimediaService.getOne(id)
            ?: throw NotFoundException(ErrorCode.NotFound, "Multimedia $id not found!")

        if (!multimedia.isViewableBy(personId)) throw DefaultNotFoundException()
        return multimedia
    }
}
