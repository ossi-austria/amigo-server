package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.domain.services.sendables.MultimediaService
import org.ossiaustria.amigo.platform.exceptions.BadRequestException
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/v1/multimedias", produces = ["application/json"], consumes = ["application/json"])
internal class MultimediasApi(private val multimediaService: MultimediaService) {

    private val serviceWrapper = SendableApiWrapper(multimediaService)

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
        @RequestParam(value = "senderId") senderId: UUID,
        @RequestParam(value = "receiverId") receiverId: UUID,
        @RequestParam(value = "albumId", required = false) albumId: UUID? = null,
        @RequestPart(value = "name", required = false) name: String?,
        @RequestPart("file") file: MultipartFile,
    ): MultimediaDto {
//        return MultimediaDto(randomUUID(),randomUUID(),randomUUID(),randomUUID(), "file", "MultimediaType.IMAGE")
        return multimediaService.createMultimedia(senderId, receiverId, albumId, name, file).toDto()
    }

    @PostMapping("/{id}/file")
    fun uploadFile(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
        @RequestPart("file") file: MultipartFile
    ): Multimedia {
        val multimedia = serviceWrapper.getOne(tokenUserDetails, id)
        return multimediaService.uploadFile(multimedia, file)
    }

    @GetMapping("/{id}/file")
    fun downloadFile(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
    ): ResponseEntity<Resource> {
        try {

            val multimedia = serviceWrapper.getOne(tokenUserDetails, id)
            val resource = multimediaService.loadFile(multimedia)

            val filename = multimedia.filename()
            val header = "attachment; filename=$filename"
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, header)
                .header(HttpHeaders.CONTENT_TYPE, multimedia.contentType)
                .header(HttpHeaders.CONTENT_LENGTH, multimedia.size.toString())
                .body(resource)
        } catch (e: Exception) {
            throw BadRequestException(ErrorCode.NotFound, e.message!!)
        }
    }

    /**
     * The following typical CRUD use cases are all handled by SendableApiWrapper for all Sendables
     */

    @GetMapping("/filter")
    fun getFiltered(
        @RequestParam(value = "receiverId", required = false) receiverId: UUID?,
        @RequestParam(value = "senderId", required = false) senderId: UUID?,
        account: Account
    ) = serviceWrapper.getFiltered(receiverId, senderId, account).map(Multimedia::toDto)

    @GetMapping("/received")
    fun getReceived(tokenUserDetails: TokenUserDetails) =
        serviceWrapper.getReceived(tokenUserDetails).map(Multimedia::toDto)

    @GetMapping("/sent")
    fun getSent(tokenUserDetails: TokenUserDetails) =
        serviceWrapper.getSent(tokenUserDetails).map(Multimedia::toDto)

    @GetMapping("/{id}")
    fun getOne(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
    ) = serviceWrapper.getOne(tokenUserDetails, id).toDto()

    @PatchMapping("/{id}/set-retrieved")
    fun markAsRetrieved(tokenUserDetails: TokenUserDetails, @PathVariable(value = "id") id: UUID) =
        serviceWrapper.markAsRetrieved(tokenUserDetails, id).toDto()

}
