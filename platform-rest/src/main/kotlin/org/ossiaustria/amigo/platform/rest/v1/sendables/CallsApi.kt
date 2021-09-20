package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.enums.CallType
import org.ossiaustria.amigo.platform.domain.services.sendables.CallService
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.MethodNotAllowedException
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v1/calls", produces = ["application/json"], consumes = ["application/json"])
internal class CallsApi(private val callService: CallService) {

    private val serviceWrapper = SendableApiWrapper(callService)

    private fun withPersonId(account: Account, personId: UUID? = null) =
        account.person(personId).id

    @PostMapping("")
    fun createCall(
        @RequestParam(value = "receiverId") receiverId: UUID,
        @RequestParam(value = "callType") callType: CallType,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ): CallTokenDto {
        val senderId = account.person(personId).id
        return callService.createCall(senderId, receiverId, callType).toTokenDto(senderId)
    }

    @PatchMapping("/{id}/cancel")
    fun cancelCall(
        @PathVariable("id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) = callWithPermissions(withPersonId(account, personId), id, matchSender = true).let { callService.cancelCall(it) }
        .toDto()

    @PatchMapping("/{id}/deny")
    fun denyCall(
        @PathVariable("id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) =
        callWithPermissions(withPersonId(account, personId), id, matchReceiver = true).let { callService.denyCall(it) }
            .toDto()


    @PatchMapping("/{id}/accept")
    fun acceptCall(
        @PathVariable("id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ): CallTokenDto {
        val uuid = withPersonId(account, personId)
        val call = callService.acceptCall(callWithPermissions(uuid, id, matchReceiver = true))
        return call.toTokenDto(uuid)
    }


    @PatchMapping("/{id}/finish")
    fun finishCall(
        @PathVariable("id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) =
        callWithPermissions(
            withPersonId(account, personId),
            id,
            matchReceiver = true
        ).let { callService.finishCall(it) }.toDto()


    fun callWithPermissions(
        personId: UUID,
        id: UUID,
        matchSender: Boolean = false,
        matchReceiver: Boolean = false,
    ) = serviceWrapper.getOne(personId, id).also {
        if (matchSender && personId != it.senderId) {
            throw MethodNotAllowedException(ErrorCode.CallChangeNotSenderError, "Must be sender of this Call!")
        }
        if (matchReceiver && personId != it.receiverId) {
            throw MethodNotAllowedException(ErrorCode.CallChangeNotReceiverError, "Must be receiver of this Call!")
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
    ) = serviceWrapper.getFiltered(receiverId, senderId, account).map(Call::toDto)

    @GetMapping("/received")
    fun getReceived(
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) =
        serviceWrapper.getReceived(withPersonId(account, personId)).map(Call::toDto)

    @GetMapping("/all")
    fun getOwn(
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) =
        serviceWrapper.getOwn(withPersonId(account, personId)).map(Call::toDto)


    @GetMapping("/sent")
    fun getSent(
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) =
        serviceWrapper.getSent(withPersonId(account, personId)).map(Call::toDto)


    @GetMapping("/{id}")
    fun getOne(
        @PathVariable(value = "id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
    ) = withPersonId(account, personId).let {
        serviceWrapper.getOne(it, id).toTokenDto(it)
    }
}
