package org.ossiaustria.amigo.platform.rest.v1.sendables

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.enums.CallType
import org.ossiaustria.amigo.platform.domain.services.sendables.CallService
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.MethodNotAllowedException
import org.ossiaustria.amigo.platform.rest.v1.common.Headers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/calls", produces = ["application/json"])
internal class CallsApi(private val callService: CallService) {

    private val serviceWrapper = SendableApiWrapper(callService)

    private fun withPersonId(account: Account, personId: UUID? = null) =
        account.person(personId).id

    @ApiOperation("Create a new Call")
    @PostMapping
    fun createCall(
        @RequestParam(value = "receiverId")
        receiverId: UUID,

        @RequestParam(value = "callType")
        callType: CallType,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): CallTokenDto {
        val senderId = account.person(personId).id
        return callService.createCall(senderId, receiverId, callType).toTokenDto(senderId)
    }

    @ApiOperation("Cancel an existing Call")
    @PatchMapping("/{id}/cancel")
    fun cancelCall(
        @PathVariable("id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) = callWithPermissions(withPersonId(account, personId), id, matchSender = true).let { callService.cancelCall(it) }
        .toDto()

    @ApiOperation("Deny an incoming Call")
    @PatchMapping("/{id}/deny")
    fun denyCall(
        @PathVariable("id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) =
        callWithPermissions(withPersonId(account, personId), id, matchReceiver = true).let { callService.denyCall(it) }
            .toDto()

    @ApiOperation("Accept an incoming Call")
    @PatchMapping("/{id}/accept")
    fun acceptCall(
        @PathVariable("id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ): CallTokenDto {
        val uuid = withPersonId(account, personId)
        val call = callService.acceptCall(callWithPermissions(uuid, id, matchReceiver = true))
        return call.toTokenDto(uuid)
    }

    @ApiOperation("Finish an active Call")
    @PatchMapping("/{id}/finish")
    fun finishCall(
        @PathVariable("id")
        id: UUID,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) =
        callWithPermissions(
            withPersonId(account, personId),
            id,
            matchReceiver = true
        ).let { callService.finishCall(it) }.toDto()


    internal fun callWithPermissions(
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
    @ApiOperation("Filter all Calls for receiver and/or sender")
    @GetMapping("/filter")
    fun getFiltered(
        @RequestParam(value = "receiverId", required = false)
        receiverId: UUID?,

        @RequestParam(value = "senderId", required = false)
        senderId: UUID?,

        @ApiParam(hidden = true)
        account: Account
    ) = serviceWrapper.getFiltered(receiverId, senderId, account).map(Call::toDto)

    @ApiOperation("Get all received Calls")
    @GetMapping("/received")
    fun getReceived(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) =
        serviceWrapper.getReceived(withPersonId(account, personId)).map(Call::toDto)

    @ApiOperation("Get all Calls")
    @GetMapping("/all")
    fun getOwn(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) =
        serviceWrapper.getOwn(withPersonId(account, personId)).map(Call::toDto)

    @ApiOperation("Get all sent Calls")
    @GetMapping("/sent")
    fun getSent(
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null,

        @ApiParam(hidden = true)
        account: Account,
    ) =
        serviceWrapper.getSent(withPersonId(account, personId)).map(Call::toDto)

    @ApiOperation("Get one Call")
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
        serviceWrapper.getOne(it, id).toTokenDto(it)
    }
}
