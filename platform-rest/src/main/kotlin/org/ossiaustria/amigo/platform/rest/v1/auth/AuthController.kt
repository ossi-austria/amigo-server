package org.ossiaustria.amigo.platform.rest.v1.auth


import org.ossiaustria.amigo.platform.services.auth.AuthService
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@RestController
@RequestMapping("/api/v1/auth", produces = ["application/json"], consumes = ["application/json"])
class AuthController(
    val authService: AuthService,
    val currentUserService: CurrentUserService
) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): AccountDto = authService
        .loginUser(
            plainPassword = loginRequest.password,
            username = loginRequest.username,
            email = loginRequest.email
        )
        .toDto()

    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): SecretAccountDto = authService
        .registerUser(
            plainPassword = registerRequest.password,
            username = registerRequest.username,
            email = registerRequest.email
        )
        .toSecretUserDto()


    @GetMapping("/whoami")
    fun whoami(): AccountDto = currentUserService.account().toDto()

}

data class LoginRequest(
    val username: String?,
    @get:Email val email: String?,
    @get:NotEmpty val password: String
)

data class RegisterRequest(
    @get:NotEmpty val username: String,
    @get:Email @get:NotEmpty val email: String,
    @get:NotEmpty val password: String,
    @get:NotEmpty val name: String
)

data class UpdateRequest(
    @get:NotEmpty val username: String? = null,
    @get:Email @get:NotEmpty val email: String? = null,
    val name: String? = null,
    val termsAcceptedAt: ZonedDateTime? = null,
    val hasNewsletters: Boolean? = null
)
