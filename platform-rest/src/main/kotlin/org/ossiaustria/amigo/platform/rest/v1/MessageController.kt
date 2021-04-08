package org.ossiaustria.amigo.platform.rest.v1

import org.ossiaustria.amigo.platform.rest.v1.sendables.MessageDto
import org.ossiaustria.amigo.platform.rest.v1.sendables.toDto
import org.ossiaustria.amigo.platform.services.MessageService
import org.ossiaustria.amigo.platform.services.SecurityService
import org.ossiaustria.amigo.platform.services.auth.TokenDetails
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/v1/messages", produces = ["application/json"], consumes = ["application/json"])
internal class MessageController(
    private val messageService: MessageService,
    private val securityService: SecurityService
) {

    @GetMapping("/filter")
    fun filter(
        @RequestParam(value = "receiverId", required = false) receiverId: UUID?,
        @RequestParam(value = "senderId", required = false) senderId: UUID?,
        tokenDetails: TokenDetails,
        account: Account,
    ): List<MessageDto> {
       val isReceiver = securityService.hasPersonId(account,receiverId)
       val isSender = securityService.hasPersonId(account,senderId)
        if (!isReceiver && !isSender) {
            throw NotFoundException(ErrorCode.AccessDenied, "Use receiverId or senderId with a Person of yours")
        }
        return messageService.findWithPersons(receiverId, senderId).map(Message::toDto)
    }
}
