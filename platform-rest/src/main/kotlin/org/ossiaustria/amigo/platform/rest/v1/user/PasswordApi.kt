package org.ossiaustria.amigo.platform.rest.v1.user

import org.ossiaustria.amigo.platform.domain.services.auth.PasswordService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v1/auth/password", produces = ["application/json"], consumes = ["application/json"])
class PasswordApi(
    private val passwordService: PasswordService
) {
    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun passwordReset(
        @RequestParam(value = "email", required = false) email: String?,
        @RequestParam(value = "user_name", required = false) userName: String?,
        @RequestParam(value = "user_id", required = false) userId: UUID?
    ) {
        passwordService.resetPasswordStart(email, userName, userId)
    }

    @PostMapping("/reset/confirm")
    fun passwordResetConfirmation(@RequestBody request: PasswordResetRequest) = passwordService.passwordResetConfirm(
        request.token,
        request.password
    )

}

class PasswordResetRequest(
    val token: String,
    val password: String
)


