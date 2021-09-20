package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.services.multimedia.MultimediaService
import org.ossiaustria.amigo.platform.domain.services.sendables.MessageService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/v1/messages", produces = ["application/json"], consumes = ["application/json"])
internal class MessagesApi(
    private val messageService: MessageService,
    private val multimediaService: MultimediaService
) {

    private val serviceWrapper = SendableApiWrapper(messageService)

    private fun withPersonId(account: Account, personId: UUID? = null) =
        account.person(personId).id

    @PostMapping(
        "", consumes = [
            MediaType.IMAGE_GIF_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun createMultimediaMessage(
        @RequestParam(value = "receiverId") receiverId: UUID,
        @RequestPart("text") text: String,
        @RequestPart("file", required = false) file: MultipartFile? = null,
        @RequestHeader("Amigo-Person-Id",required = false) personId: UUID? = null,
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
    fun getOwn(
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) =
        serviceWrapper.getOwn(withPersonId(account, personId)).map(Message::toDto)


    @GetMapping("/filter")
    fun getFiltered(
        @RequestParam(value = "receiverId", required = false) receiverId: UUID?,
        @RequestParam(value = "senderId", required = false) senderId: UUID?,
        account: Account
    ) = serviceWrapper.getFiltered(receiverId, senderId, account).map(Message::toDto)

    @GetMapping("/received")
    fun getReceived(
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) = withPersonId(account, personId).let {
        serviceWrapper.getReceived(it).map(Message::toDto)
    }

    @GetMapping("/sent")
    fun getSent(
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) = withPersonId(account, personId).let {
        serviceWrapper.getSent(it).map(Message::toDto)
    }


    @GetMapping("/{id}")
    fun getOne(
        @PathVariable(value = "id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) = withPersonId(account, personId).let {
        serviceWrapper.getOne(it, id).toMultimediaDto()
    }

    @PatchMapping("/{id}/set-retrieved")
    fun markAsRetrieved(
        @PathVariable(value = "id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) =
        serviceWrapper.markAsRetrieved(withPersonId(account, personId), id).toMultimediaDto()

}
