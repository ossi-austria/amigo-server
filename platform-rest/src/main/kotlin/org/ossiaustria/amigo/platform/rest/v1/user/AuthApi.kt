package org.ossiaustria.amigo.platform.rest.v1.user


import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@RestController
@RequestMapping("/v1/auth", produces = ["application/json"])
class AuthApi(
    val authService: AuthService,
    val currentUserService: CurrentUserService
) {

    @ApiOperation("Log in with existing Account")
    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequest
    ): LoginResultDto = authService
        .loginUser(
            email = loginRequest.email,
            plainPassword = loginRequest.password
        )
        .toDto()

    @ApiOperation("Refresh the AccessToken via valid RefreshToken for an Account")
    @PostMapping("/refresh-token")
    fun refreshToken(
        @RequestBody refreshAccessTokenRequest: RefreshAccessTokenRequest
    ): TokenResultDto = authService
        .refreshAccessToken(refreshToken = refreshAccessTokenRequest.refreshToken)
        .toDto()

    @ApiOperation("Register a new Account")
    @PostMapping("/register")
    fun register(
        @RequestBody registerRequest: RegisterRequest
    ): AccountDto = authService
        .registerAccount(
            email = registerRequest.email,
            plainPassword = registerRequest.password,
            name = registerRequest.name,
            optionalGroupId = registerRequest.optionalGroupId
        )
        .toDto()

    @ApiOperation("Register a new Analogue-Account")
    @PostMapping("/register-analogue")
    fun registerAnalogue(
        @ApiParam(hidden = true)
        account: Account,

        @RequestBody registerAnalogueRequest: RegisterAnalogueRequest
    ): SecretAccountDto = authService
        .registerAnalogueAccount(
            creator = account,
            name = registerAnalogueRequest.name,
            neededGroupId = registerAnalogueRequest.neededGroupId
        )
        .toSecretUserDto()

    @ApiOperation("Get own Account information")
    @GetMapping("/account")
    fun account(): SecretAccountDto = currentUserService.account().toSecretUserDto()

    @ApiOperation("Update Firebase Cloud Messaging token for own Account")
    @PostMapping("/fcm-token")
    fun setFcmToken(
        @ApiParam(hidden = true) tokenUserDetails: TokenUserDetails,
        @RequestBody setFcmTokenRequest: SetFcmTokenRequest
    ) {
        authService.setFcmToken(tokenUserDetails.accountId, setFcmTokenRequest.fcmToken)
    }
}

data class LoginRequest(
    @get:Email val email: String,
    @get:NotEmpty val password: String
)

data class RefreshAccessTokenRequest(val refreshToken: String)

@ApiModel("Register a new Account and attach to explicit or implicit Group with a Person")
data class RegisterRequest(
    @ApiModelProperty("Valid unused email", required = true)
    @get:Email @get:NotEmpty val email: String,
    @ApiModelProperty("Password. Keep it safe", required = true)
    @get:NotEmpty val password: String,
    @ApiModelProperty("Not-blank firstname, fullname or spitzname", required = true)
    @get:NotEmpty val name: String,
    @ApiModelProperty("When given: attaches to existing Group. If not: creating a new implicit Group")
    val optionalGroupId: UUID? = null
)

@ApiModel("Register a new Analogue-Account and attach to explicit Group with a Person")
data class RegisterAnalogueRequest(
    @ApiModelProperty("Not-blank firstname, fullname or spitzname", required = true)
    @get:NotEmpty val name: String,

    @ApiModelProperty("Attaches to existing Group of Owner")
    val neededGroupId: UUID
)

@ApiModel("Provide Firebase Cloud Messaging Token for update Push subscriptions")
data class SetFcmTokenRequest(val fcmToken: String)
