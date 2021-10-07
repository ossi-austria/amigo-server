package org.ossiaustria.amigo.platform.rest.v1.user


import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/account", produces = ["application/json"], consumes = ["application/json"])
class AccountApi(
    val authService: AuthService,
    val currentUserService: CurrentUserService
) {

    @ApiOperation("Update Firebase Cloud Messaging token for own Account")
    @PostMapping("/set-fcm-token")
    fun setFcmToken(
        @ApiParam(hidden = true) tokenUserDetails: TokenUserDetails,
        @RequestBody setFcmTokenRequest: SetFcmTokenRequest
    ) {
        authService.setFcmToken(tokenUserDetails.accountId, setFcmTokenRequest.fcmToken)
    }

}

data class SetFcmTokenRequest(val fcmToken: String)
