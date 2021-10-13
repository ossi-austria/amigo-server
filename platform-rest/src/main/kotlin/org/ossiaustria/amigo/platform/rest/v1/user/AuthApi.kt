package org.ossiaustria.amigo.platform.rest.v1.user


import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import io.swagger.annotations.ApiOperation
import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
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
    ): SecretAccountDto = authService
        .registerUser(
            email = registerRequest.email,
            plainPassword = registerRequest.password,
            name = registerRequest.name,
            optionalGroupId = registerRequest.optionalGroupId
        )
        .toSecretUserDto()

    @ApiOperation("DEPRECATED, get own Account information")
    @GetMapping("/whoami")
    fun whoami(): AccountDto = currentUserService.account().toDto()

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

data class UpdateRequest(
    val name: String? = null,
)
