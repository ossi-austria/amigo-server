package org.ossiaustria.amigo.platform.rest.v1.sendables

import io.micrometer.core.annotation.Timed
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.domain.services.sendables.MultimediaService
import org.ossiaustria.amigo.platform.exceptions.BadRequestException
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Timed
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
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
        @RequestPart("file") file: MultipartFile
    ): Multimedia {
        val multimedia = getOneEntity(tokenUserDetails, id)
        return multimediaService.uploadFile(multimedia, file)
    }

    @GetMapping("/{id}/file")
    fun downloadFile(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
    ): ResponseEntity<Resource> {
        try {
            val multimedia = getOneEntity(tokenUserDetails, id)
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
    fun getOwn(tokenUserDetails: TokenUserDetails): List<MultimediaDto> {
        val receiverId = tokenUserDetails.personsIds.first()
        return multimediaService.findWithOwner(receiverId).map(Multimedia::toDto)
    }

    @GetMapping("/{id}")
    fun getOne(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
    ): MultimediaDto = getOneEntity(tokenUserDetails, id).toDto()

    private fun getOneEntity(
        tokenUserDetails: TokenUserDetails,
        id: UUID
    ): Multimedia {
        val personId = tokenUserDetails.personsIds.first()
        val multimedia = multimediaService.getOne(id)
        if (!multimedia.isViewableBy(personId)) throw DefaultNotFoundException()
        return multimedia
    }
}
