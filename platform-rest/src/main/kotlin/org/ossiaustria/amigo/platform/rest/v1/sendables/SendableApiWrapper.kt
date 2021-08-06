package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Sendable
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.domain.services.sendables.SendableService
import org.ossiaustria.amigo.platform.exceptions.BadRequestException
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import java.time.ZonedDateTime
import java.util.*


internal class SendableApiWrapper<T : Sendable<T>>(
    private val sendableService: SendableService<T>,
) {

    fun getFiltered(receiverId: UUID?, senderId: UUID?, account: Account): List<T> {
        val isReceiver = account.hasPersonId(receiverId)
        val isSender = account.hasPersonId(senderId)
        if (!isReceiver && !isSender) {
            throw BadRequestException(ErrorCode.BadParametersRequest, "Use receiverId=you or senderId=you")
        }
        return sendableService.findWithPersons(senderId, receiverId)
    }

    fun getReceived(tokenUserDetails: TokenUserDetails): List<T> {
        val receiverId = tokenUserDetails.personsIds.first()
        return sendableService.findWithReceiver(receiverId)
    }

    fun getSent(tokenUserDetails: TokenUserDetails): List<T> {
        val receiverId = tokenUserDetails.personsIds.first()
        return sendableService.findWithSender(receiverId)
    }

    fun getOne(tokenUserDetails: TokenUserDetails, id: UUID): T {
        val personId = tokenUserDetails.personsIds.first()
        val sendable =
            sendableService.getOne(id) ?: throw NotFoundException(ErrorCode.NotFound, "Sendable $id not found!")
        if (!sendable.isViewableBy(personId)) throw DefaultNotFoundException()
        return sendable
    }

    fun markAsRetrieved(tokenUserDetails: TokenUserDetails, id: UUID): T {
        val personId = tokenUserDetails.personsIds.first()
        val item = sendableService.getOne(id) ?: throw NotFoundException(ErrorCode.NotFound, "Sendable $id not found!")
        if (!item.isRetrievableBy(personId)) throw DefaultNotFoundException()
        return sendableService.markAsRetrieved(id, ZonedDateTime.now())
    }

}
