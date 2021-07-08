package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.enums.CallType
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.domain.services.sendables.CallService
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.MethodNotAllowedException
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v1/calls", produces = ["application/json"], consumes = ["application/json"])
internal class CallsApi(private val callService: CallService) {

    private val serviceWrapper = SendableApiWrapper(callService)

    @PostMapping("")
    fun createCall(
        @RequestParam(value = "senderId") senderId: UUID,
        @RequestParam(value = "receiverId") receiverId: UUID,
        @RequestParam(value = "callType") callType: CallType,
    ): CallTokenDto {
        return callService.createCall(senderId, receiverId, callType).toTokenDto(senderId)
    }

    @PatchMapping("/{id}/cancel")
    fun cancelCall(userDetails: TokenUserDetails, @PathVariable(value = "id") id: UUID) =
        callWithPermissions(userDetails, id, matchSender = true).let { callService.cancelCall(it) }.toDto()

    @PatchMapping("/{id}/deny")
    fun denyCall(userDetails: TokenUserDetails, @PathVariable(value = "id") id: UUID) =
        callWithPermissions(userDetails, id, matchReceiver = true).let { callService.denyCall(it) }.toDto()

    @PatchMapping("/{id}/accept")
    fun acceptCall(userDetails: TokenUserDetails, @PathVariable(value = "id") id: UUID) =
        callWithPermissions(userDetails, id, matchReceiver = true).let { callService.acceptCall(it) }
            .toTokenDto(userDetails.personId())

    @PatchMapping("/{id}/finish")
    fun finishCall(userDetails: TokenUserDetails, @PathVariable(value = "id") id: UUID) =
        callWithPermissions(userDetails, id).let { callService.finishCall(it) }.toDto()


    fun callWithPermissions(
        tokenUserDetails: TokenUserDetails,
        id: UUID,
        matchSender: Boolean = false,
        matchReceiver: Boolean = false,
    ) = serviceWrapper.getOne(tokenUserDetails, id).also {
        val personId = tokenUserDetails.personId()
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
    fun getReceived(tokenUserDetails: TokenUserDetails) =
        serviceWrapper.getReceived(tokenUserDetails).map(Call::toDto)

    @GetMapping("/sent")
    fun getSent(tokenUserDetails: TokenUserDetails) =
        serviceWrapper.getSent(tokenUserDetails).map(Call::toDto)

    @GetMapping("/{id}")
    fun getOne(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
    ) = serviceWrapper.getOne(tokenUserDetails, id).toTokenDto(tokenUserDetails.personId())

}
