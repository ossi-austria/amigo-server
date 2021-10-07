package org.ossiaustria.amigo.platform.rest.v1.sendables

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import io.swagger.annotations.Authorization
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.services.multimedia.MultimediaService
import org.ossiaustria.amigo.platform.domain.services.sendables.MessageService
import org.ossiaustria.amigo.platform.rest.v1.common.Headers
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID


@Api("REST for Messages and sending")
@RestController
@RequestMapping("/v1/messages", produces = ["application/json"], consumes = ["application/json"])
internal class MessagesApi(
    private val messageService: MessageService,
    private val multimediaService: MultimediaService
) {

    private val serviceWrapper = SendableApiWrapper(messageService)

    private fun withPersonId(account: Account, personId: UUID? = null) =
        account.person(personId).id

    @ApiOperation("Create a new Message with attached Multimedia/File")
    @PostMapping(
        consumes = [
            MediaType.IMAGE_GIF_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun createMultimediaMessage(
        @RequestParam(value = "receiverId")
        receiverId: UUID,

        @RequestPart("text")
        text: String,

        @RequestPart("file", required = false)
        file: MultipartFile? = null,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): MultiMessageDto {
        val senderId = account.person(personId).id
        val multimedia = file?.let {
            multimediaService.createMultimedia(senderId, null, System.currentTimeMillis().toString(), it)
        }
        return messageService.createMessage(senderId, receiverId, text, multimedia).toMultimediaDto()
    }

    /**
     * The following typical CRUD use cases are all handled by SendableApiWrapper for all Sendables
     */

    @GetMapping("/all")
    @ApiResponses(value = [
        ApiResponse(code = 200, message = "Successfully retrieved list"),
        ApiResponse(code = 401, message = "You are not authorized to view the resource"),
        ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
        ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    ]
    )
    fun getOwn(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) =
        serviceWrapper.getOwn(withPersonId(account, personId)).map(Message::toDto)

    @ApiOperation("Filter all Messages for receiver and/or sender")
    @GetMapping("/filter")
    fun getFiltered(
        @RequestParam(value = "receiverId", required = false)
        receiverId: UUID?,

        @RequestParam(value = "senderId", required = false)
        senderId: UUID?,

        @ApiParam(hidden = true)
        account: Account
    ) = serviceWrapper.getFiltered(receiverId, senderId, account).map(Message::toDto)

    @ApiOperation("Get all received Messages")
    @GetMapping("/received")
    fun getReceived(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) = withPersonId(account, personId).let {
        serviceWrapper.getReceived(it).map(Message::toDto)
    }

    @ApiOperation("Get all sent Messages")
    @GetMapping("/sent")
    fun getSent(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) = withPersonId(account, personId).let {
        serviceWrapper.getSent(it).map(Message::toDto)
    }

    @ApiOperation("Get one Message")
    @GetMapping("/{id}")
    fun getOne(
        @PathVariable(value = "id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) = withPersonId(account, personId).let {
        serviceWrapper.getOne(it, id).toMultimediaDto()
    }

    @PatchMapping("/{id}/set-retrieved")
    fun markAsRetrieved(
        @PathVariable(value = "id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) =
        serviceWrapper.markAsRetrieved(withPersonId(account, personId), id).toMultimediaDto()

}
