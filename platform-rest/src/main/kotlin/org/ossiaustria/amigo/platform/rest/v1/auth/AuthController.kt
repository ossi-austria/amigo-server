package org.ossiaustria.amigo.platform.rest.v1.auth


import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.ossiaustria.amigo.platform.services.auth.AuthService
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@RestController
@RequestMapping("/v1/auth", produces = ["application/json"], consumes = ["application/json"])
class AuthController(
    val authService: AuthService,
    val currentUserService: CurrentUserService
) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): LoginResultDto = authService
        .loginUser(
            email = loginRequest.email,
            plainPassword = loginRequest.password
        )
        .toDto()

    @PostMapping("/refresh-token")
    fun refreshToken(@RequestBody refreshAccessTokenRequest: RefreshAccessTokenRequest): TokenResultDto = authService
        .refreshAccessToken(refreshToken = refreshAccessTokenRequest.refreshToken)
        .toDto()

    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): SecretAccountDto = authService
        .registerUser(
            email = registerRequest.email,
            plainPassword = registerRequest.password,
            name = registerRequest.name
        )
        .toSecretUserDto()

    @GetMapping("/whoami")
    fun whoami(): AccountDto = currentUserService.account().toDto()

}

data class LoginRequest(
    @get:Email val email: String,
    @get:NotEmpty val password: String
)

data class RefreshAccessTokenRequest(
    val refreshToken: String
)

data class RegisterRequest(
    @get:Email @get:NotEmpty val email: String,
    @get:NotEmpty val password: String,
    @get:NotEmpty val name: String
)

data class UpdateRequest(
    val name: String? = null,
)
