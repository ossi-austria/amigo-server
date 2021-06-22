package org.ossiaustria.amigo.platform.rest.v1.auth


import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/account", produces = ["application/json"], consumes = ["application/json"])
class AccountController(
    val authService: AuthService,
    val currentUserService: CurrentUserService
) {

    @PostMapping("/set-fcm-token")
    fun setFcmToken(
        tokenUserDetails: TokenUserDetails,
        @RequestBody setFcmTokenRequest: SetFcmTokenRequest
    ) {
        authService.setFcmToken(tokenUserDetails.accountId, setFcmTokenRequest.fcmToken)
    }

}

data class SetFcmTokenRequest(val fcmToken: String)
