package org.ossiaustria.amigo.platform.rest.v1.user

import io.swagger.annotations.ApiOperation
import org.ossiaustria.amigo.platform.domain.services.auth.PasswordService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/auth/password", produces = ["application/json"])
class PasswordApi(
    private val passwordService: PasswordService
) {
    @ApiOperation("UNSTABLE: Begin password reset process for Account")
    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun passwordReset(
        @RequestParam(value = "email", required = false)
        email: String?,

        @RequestParam(value = "user_name", required = false)
        userName: String?,

        @RequestParam(value = "user_id", required = false)
        userId: UUID?
    ) {
        passwordService.resetPasswordStart(email, userName, userId)
    }

    @ApiOperation("UNSTABLE: Confirm password reset for Account")
    @PostMapping("/reset/confirm")
    fun passwordResetConfirmation(@RequestBody request: PasswordResetRequest) =
        passwordService.passwordResetConfirm(
            request.token,
            request.password
        )

}

class PasswordResetRequest(
    val token: String,
    val password: String
)


