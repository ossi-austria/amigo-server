package org.ossiaustria.amigo.platform.rest.v1

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.services.MessageService
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.exceptions.BadRequestException
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.rest.v1.sendables.MessageDto
import org.ossiaustria.amigo.platform.rest.v1.sendables.toDto
import org.ossiaustria.amigo.platform.services.SecurityService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/v1/messages", produces = ["application/json"], consumes = ["application/json"])
internal class MessageController(
    private val messageService: MessageService,
    private val securityService: SecurityService,
    private val accountRepository: AccountRepository,
) {

    @GetMapping("/filter")
    fun filter(
        @RequestParam(value = "receiverId", required = false) receiverId: UUID?,
        @RequestParam(value = "senderId", required = false) senderId: UUID?,
        tokenUserDetails: TokenUserDetails,
        account: Account,
    ): List<MessageDto> {
        val findById = accountRepository.findByIdOrNull(account.id)!!
        val isReceiver = securityService.hasPersonId(findById, receiverId)
        val isSender = securityService.hasPersonId(findById, senderId)
        if (!isReceiver && !isSender) {
            throw BadRequestException(
                ErrorCode.BadParametersRequest,
                "Use receiverId or senderId with a Person of yours"
            )
        }
        return messageService.findWithPersons(receiverId, senderId).map(Message::toDto)
    }
}
