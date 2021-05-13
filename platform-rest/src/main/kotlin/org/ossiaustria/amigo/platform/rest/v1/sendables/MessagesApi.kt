package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.domain.services.sendables.MessageService
import org.ossiaustria.amigo.platform.exceptions.BadRequestException
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/v1/messages", produces = ["application/json"], consumes = ["application/json"])
internal class MessagesApi(
    private val messageService: MessageService,
) {

    @GetMapping("/filter")
    fun getFiltered(
        @RequestParam(value = "receiverId", required = false) receiverId: UUID?,
        @RequestParam(value = "senderId", required = false) senderId: UUID?,
        account: Account
    ): List<MessageDto> {
        val isReceiver = account.hasPersonId(receiverId)
        val isSender = account.hasPersonId(senderId)
        if (!isReceiver && !isSender) {
            throw BadRequestException(ErrorCode.BadParametersRequest, "Use receiverId=you or senderId=you")
        }
        val findWithPersons = messageService.findWithPersons(senderId, receiverId)
        return findWithPersons.map(Message::toDto)
    }

    @GetMapping("/received")
    fun getReceived(tokenUserDetails: TokenUserDetails): List<MessageDto> {
        val receiverId = tokenUserDetails.personsIds.first()
        return messageService.findWithReceiver(receiverId).map(Message::toDto)
    }

    @GetMapping("/sent")
    fun getSent(tokenUserDetails: TokenUserDetails): List<MessageDto> {
        val receiverId = tokenUserDetails.personsIds.first()
        return messageService.findWithSender(receiverId).map(Message::toDto)
    }

    @GetMapping("/{id}")
    fun getOne(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
    ): MessageDto {
        val personId = tokenUserDetails.personsIds.first()
        val sendable = messageService.getOne(id)
        if (!sendable.isViewableBy(personId)) throw DefaultNotFoundException()
        return sendable.toDto()
    }

    @PatchMapping("/{id}/set-retrieved")
    fun performAction(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
    ): MessageDto {
        val personId = tokenUserDetails.personsIds.first()
        val item = messageService.getOne(id)
        if (!item.isRetrievableBy(personId)) throw DefaultNotFoundException()
        val updated = messageService.markAsRetrieved(id, ZonedDateTime.now())
        return updated.toDto()
    }

}
