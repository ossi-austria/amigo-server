package org.ossiaustria.amigo.platform.domain.services.auth

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.exceptions.ConflictException
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.UnknownUserException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

interface PasswordService {
    fun resetPasswordStart(email: String? = null, userName: String? = null, userId: UUID? = null)
    fun passwordResetConfirm(token: String, password: String): Boolean
}

@Service
class APPasswordService(
    private val accountRepository: AccountRepository,
    private val authService: AuthService,
) : PasswordService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        private const val RESET_PASSWORD_SUBJECT = "Password reset"
    }

    override fun resetPasswordStart(email: String?, userName: String?, userId: UUID?) {
        val account: Account

        when {
            email != null -> account = accountRepository.findOneByEmail(email)
                ?: throw UnknownUserException()
            userName != null -> account = accountRepository.findOneByEmail(userName)
                ?: throw UnknownUserException()
            userId != null -> account = accountRepository.findByIdOrNull(userId)
                ?: throw UnknownUserException()
            else -> throw ConflictException(ErrorCode.UserBadCredentials, "Any search parameter should be presented")
        }

        if (account.changeAccountToken != null &&
            account.changeAccountTokenCreatedAt != null &&
            account.changeAccountTokenCreatedAt!!.plusSeconds(20).isAfter(ZonedDateTime.now())
        ) {
            val nextSendTime =
                DateTimeFormatter.ISO_ZONED_DATE_TIME.format(account.changeAccountTokenCreatedAt!!.plusSeconds(20))
            log.warn("Password reset email has already been sent. The next sending possible after $nextSendTime")
            return
        }

//        val savedAccount = accountRepository.save(
//            account.copy(
//                changeAccountToken = UUID.randomUUID().toString(),
//                changeAccountTokenCreatedAt = ZonedDateTime.now()
//            )
//        )
//
//        val resetPasswordUrl = "reset/${account.changeAccountToken}"

//        val variables = mapOf(
//            EmailVariables.USER_NAME to savedAccount.email,
//            EmailVariables.RECIPIENT_EMAIL to savedAccount.email,
//            EmailVariables.SUBJECT to RESET_PASSWORD_SUBJECT,
//            EmailVariables.REDIRECT_URL to resetPasswordUrl
//        )

//        emailService.sendAsync(savedAccount.id, EmailMessageType.HTML, TemplateType.PASSWORD_RESET_TEMPLATE, variables)
    }

    override fun passwordResetConfirm(token: String, password: String): Boolean {
        val account = accountRepository.findByChangeAccountToken(token)
            ?: throw UnknownUserException("User has not password reset requested")

        if (account.changeAccountTokenCreatedAt == null ||
            account.changeAccountTokenCreatedAt!!.plusSeconds(60).isBefore(ZonedDateTime.now())
        ) {
            throw ConflictException(
                ErrorCode.BadParametersRequest,
                "Reset password operation is expired. Please request a new one"
            )
        }

        val savedAccount = accountRepository.save(account.copy(changeAccountToken = null, changeAccountTokenCreatedAt = null))

        return authService.changePasswordForUser(savedAccount, password)
    }
}