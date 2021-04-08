package org.ossiaustria.amigo.platform.services.auth


import org.ossiaustria.amigo.platform.repositories.AccountRepository
import org.ossiaustria.amigo.platform.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.exceptions.IncorrectCredentialsException
import org.ossiaustria.amigo.platform.exceptions.UserAlreadyExistsException
import org.ossiaustria.amigo.platform.services.email.EmailMessageType
import org.ossiaustria.amigo.platform.services.email.EmailService
import org.ossiaustria.amigo.platform.services.email.EmailVariables
import org.ossiaustria.amigo.platform.services.email.TemplateType
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*
import java.util.UUID.randomUUID
import javax.transaction.Transactional


@Service("authService")
class AuthService(
    private val accountRepository: AccountRepository,
    private val personRepository: PersonRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService
) {


    companion object {
        val log = LoggerFactory.getLogger(this::class.java)

        private val GUEST_ACCOUNT_ID = UUID(0L, 0L)
        private val GUEST_PERSON_ID = UUID(0L, 0L)

        private const val WELCOME_MESSAGE_SUBJECT = "Welcome"

        private val guestTokenDetails: TokenDetails by lazy {
            TokenDetails(
                username = "",
                accessToken = "",
                accountId = GUEST_ACCOUNT_ID,
                personId = GUEST_PERSON_ID,
            )
        }
    }

    fun loginUser(plainPassword: String, username: String?, email: String?): Account {
        val byUsername: Account? = if (username != null) accountRepository.findOneByEmail(username) else null
        val byEmail: Account? = if (email != null) accountRepository.findOneByEmail(email) else null

        val found: List<Account> = listOfNotNull(byUsername, byEmail).filter { account ->
            passwordEncoder.matches(plainPassword, account.passwordEncrypted)
        }

        val account = found.getOrNull(0) ?: throw IncorrectCredentialsException("username or password is incorrect")

        val accountUpdate = account.copy(lastLogin = ZonedDateTime.now())
        return accountRepository.save(accountUpdate)
    }

    @Transactional
    fun registerUser(
        plainPassword: String, username: String, email: String
    ): Account {
        val encryptedPassword = passwordEncoder.encode(plainPassword)
        val byUsername: Account? = accountRepository.findOneByEmail(username)
        val byEmail: Account? = accountRepository.findOneByEmail(email)

        if (listOfNotNull(byUsername, byEmail).isNotEmpty()) {
            throw UserAlreadyExistsException(username, email)
        }

        val accountUuid = randomUUID()

        val person = personRepository.save(
            Person(
                id = randomUUID(),
                name = username,
                groupId = UUID.randomUUID(),
                memberType = MembershipType.MEMBER
            )
        )

        val newUser = Account(
            id = accountUuid, email = email, passwordEncrypted = encryptedPassword, persons = listOf(person)
        )

        accountRepository.save(newUser)

        sendWelcomeMessage(newUser)

        return newUser
    }


    private fun sendWelcomeMessage(account: Account) {
        val variables = mapOf(
            EmailVariables.USER_NAME to account.email,
            EmailVariables.RECIPIENT_EMAIL to account.email,
            EmailVariables.SUBJECT to WELCOME_MESSAGE_SUBJECT
        )
        emailService.sendAsync(account.id, EmailMessageType.HTML, TemplateType.WELCOME_MESSAGE_TEMPLATE, variables)
    }


    @Transactional
    fun changePasswordForUser(account: Account, newPassword: String): Boolean {

        val passwordEncrypted = passwordEncoder.encode(newPassword)

        accountRepository.save(
            account.copy(passwordEncrypted = passwordEncrypted)
        )

        return true
    }


    fun createGuestDetails() = guestTokenDetails


    fun findAccountById(id: UUID): Account? {
        return accountRepository.findByIdOrNull(id)
    }

    fun findAccountByUsername(username: String): Account? {
        return accountRepository.findOneByEmail(username)
    }


}
