package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.domain.services.sendables.MessageService
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/v1/messages", produces = ["application/json"], consumes = ["application/json"])
internal class MessagesApi(private val messageService: MessageService) {

    private val serviceWrapper = SendableApiWrapper(messageService)

    @PostMapping("")
    fun createMessage(
        @RequestParam(value = "senderId") senderId: UUID,
        @RequestParam(value = "receiverId") receiverId: UUID,
        @RequestBody text: String,
    ): MessageDto {
        val message = Message(
            id = UUID.randomUUID(),
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            createdAt = ZonedDateTime.now(),
            retrievedAt = null,
            sentAt = null,
        )
//        return message.toDto()
        return messageService.createMessage(senderId, receiverId, text).toDto()
    }

    /**
     * The following typical CRUD use cases are all handled by SendableApiWrapper for all Sendables
     */

    @GetMapping("/filter")
    fun getFiltered(
        @RequestParam(value = "receiverId", required = false) receiverId: UUID?,
        @RequestParam(value = "senderId", required = false) senderId: UUID?,
        account: Account
    ) = serviceWrapper.getFiltered(receiverId, senderId, account).map(Message::toDto)

    @GetMapping("/received")
    fun getReceived(tokenUserDetails: TokenUserDetails) =
        serviceWrapper.getReceived(tokenUserDetails).map(Message::toDto)

    @GetMapping("/sent")
    fun getSent(tokenUserDetails: TokenUserDetails) =
        serviceWrapper.getSent(tokenUserDetails).map(Message::toDto)

    @GetMapping("/{id}")
    fun getOne(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
    ) = serviceWrapper.getOne(tokenUserDetails, id).toDto()

    @PatchMapping("/{id}/set-retrieved")
    fun markAsRetrieved(tokenUserDetails: TokenUserDetails, @PathVariable(value = "id") id: UUID) =
        serviceWrapper.markAsRetrieved(tokenUserDetails, id).toDto()

}
