package org.ossiaustria.amigo.platform.rest.v1.sendables

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.domain.services.sendables.MultimediaService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v1/multimedias", produces = ["application/json"], consumes = ["application/json"])
internal class MultimediasApi(multimediaService: MultimediaService) {

    private val serviceWrapper = SendableApiWrapper(multimediaService)

    /**
     * The following typical CRUD use cases are all handled by SendableApiWrapper for all Sendables
     */

    @GetMapping("/filter")
    fun getFiltered(
        @RequestParam(value = "receiverId", required = false) receiverId: UUID?,
        @RequestParam(value = "senderId", required = false) senderId: UUID?,
        account: Account
    ) = serviceWrapper.getFiltered(receiverId, senderId, account).map(Multimedia::toDto)

    @GetMapping("/received")
    fun getReceived(tokenUserDetails: TokenUserDetails) =
        serviceWrapper.getReceived(tokenUserDetails).map(Multimedia::toDto)

    @GetMapping("/sent")
    fun getSent(tokenUserDetails: TokenUserDetails) =
        serviceWrapper.getSent(tokenUserDetails).map(Multimedia::toDto)

    @GetMapping("/{id}")
    fun getOne(
        tokenUserDetails: TokenUserDetails,
        @PathVariable(value = "id") id: UUID,
    ) = serviceWrapper.getOne(tokenUserDetails, id).toDto()

    @PatchMapping("/{id}/set-retrieved")
    fun markAsRetrieved(tokenUserDetails: TokenUserDetails, @PathVariable(value = "id") id: UUID) =
        serviceWrapper.markAsRetrieved(tokenUserDetails, id).toDto()

}
