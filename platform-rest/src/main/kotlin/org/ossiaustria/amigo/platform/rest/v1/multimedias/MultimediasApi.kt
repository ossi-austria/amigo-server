package org.ossiaustria.amigo.platform.rest.v1.multimedias

import io.micrometer.core.annotation.Timed
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.services.multimedia.MultimediaService
import org.ossiaustria.amigo.platform.exceptions.BadRequestException
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Timed(value = "time.api.multimedias")
@RestController
@RequestMapping("/v1/multimedias", produces = ["application/json"], consumes = ["application/json"])
internal class MultimediasApi(private val multimediaService: MultimediaService) {

    @PostMapping(
        "",
        consumes = [
            MediaType.IMAGE_GIF_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun createMultimedia(
        @RequestParam(value = "ownerId") ownerId: UUID,
        @RequestParam(value = "albumId", required = false) albumId: UUID? = null,
        @RequestPart(value = "name", required = false) name: String?,
        @RequestPart("file") file: MultipartFile,
    ): MultimediaDto {
        return multimediaService.createMultimedia(ownerId, albumId, name, file).toDto()
    }

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
        @RequestPart("file") file: MultipartFile,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ): Multimedia {
        val multimedia = getOneEntity(account.person(personId).id, id)
        return multimediaService.uploadFile(multimedia, file)
    }

    @GetMapping("/{id}/file")
    fun downloadFile(
        @PathVariable(value = "id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ): ResponseEntity<Resource> {
        try {
            val multimedia = getOneEntity(account.person(personId).id, id)
            val resource = multimediaService.loadFile(multimedia)

            val filename = multimedia.filename()
            val header = "attachment; filename=$filename"
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, header)
                .header(HttpHeaders.CONTENT_TYPE, multimedia.contentType)
                .header(HttpHeaders.CONTENT_LENGTH, multimedia.size?.toString() ?: "0")
                .body(resource)
        } catch (e: Exception) {
            throw BadRequestException(ErrorCode.NotFound, e.message!!)
        }
    }


    @GetMapping("/own")
    fun own(
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ): List<MultimediaDto> {
        val receiverId = account.person(personId).id
        return multimediaService.findWithOwner(receiverId).map(Multimedia::toDto)
    }

    @GetMapping("/shared")
    fun shared(
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ): List<MultimediaDto> {
        val accessorId = account.person(personId).id
        return multimediaService.findWithAccess(accessorId).map(Multimedia::toDto)
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable(value = "id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ): MultimediaDto = getOneEntity(account.person(personId).id, id).toDto()

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
