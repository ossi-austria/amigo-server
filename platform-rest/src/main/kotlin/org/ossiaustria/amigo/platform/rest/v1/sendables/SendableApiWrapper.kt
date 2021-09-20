package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Sendable
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

    fun getOwn(personId: UUID): List<T> {
        return sendableService.findWithPerson(personId)
    }

    fun getReceived(personId: UUID): List<T> {
        return sendableService.findWithReceiver(personId)
    }

    fun getSent(personId: UUID): List<T> {
        return sendableService.findWithSender(personId)
    }

    fun getOne(personId: UUID, id: UUID): T {
        val sendable =
            sendableService.getOne(id) ?: throw NotFoundException(ErrorCode.NotFound, "Sendable $id not found!")
        if (!sendable.isViewableBy(personId)) throw DefaultNotFoundException()
        return sendable
    }

    fun markAsRetrieved(personId: UUID, id: UUID): T {
        val item = sendableService.getOne(id) ?: throw NotFoundException(ErrorCode.NotFound, "Sendable $id not found!")
        if (!item.isRetrievableBy(personId)) throw DefaultNotFoundException()
        return sendableService.markAsRetrieved(id, ZonedDateTime.now())
    }

}
