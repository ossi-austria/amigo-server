package org.ossiaustria.amigo.platform.rest.v1.user


import io.swagger.annotations.ApiOperation
import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@RestController
@RequestMapping("/v1/auth", produces = ["application/json"], consumes = ["application/json"])
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
            name = registerRequest.name
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

data class RegisterRequest(
    @get:Email @get:NotEmpty val email: String,
    @get:NotEmpty val password: String,
    @get:NotEmpty val name: String
)

data class UpdateRequest(
    val name: String? = null,
)
