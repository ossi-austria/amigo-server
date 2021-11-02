package org.ossiaustria.amigo.platform.domain.services.auth


import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.EmailValidator
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.LoginToken
import org.ossiaustria.amigo.platform.domain.models.PasswordValidator
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.StringValidator
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.repositories.GroupRepository
import org.ossiaustria.amigo.platform.domain.repositories.LoginTokenRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.NameGeneratorService
import org.ossiaustria.amigo.platform.domain.services.SecurityError
import org.ossiaustria.amigo.platform.domain.services.ServiceError
import org.ossiaustria.amigo.platform.exceptions.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.UUID
import java.util.UUID.randomUUID
import javax.transaction.Transactional
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank


@Service("authService")
class AuthService(
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {
    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var loginTokenRepository: LoginTokenRepository

    @Autowired
    private lateinit var nameGeneratorService: NameGeneratorService

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)

        val GUEST_ACCOUNT_ID = UUID(0L, 0L)

        private const val DEFAULT_GROUP = "AMIGO"
        private const val AMIGO_ANALOGUE_EMAIL_SUFFIX = "@amigobox"
    }

    @Transactional
    fun loginUser(email: String, plainPassword: String): LoginResult {
        log.info("User tries to login: $email")
        val account: Account = accountRepository.findOneByEmail(email)
            ?: throw UnauthorizedException("username or password is incorrect")

        val checkPw = passwordEncoder.matches(plainPassword, account.passwordEncrypted)

        if (!checkPw) throw UnauthorizedException("username or password is incorrect")
        return generateTokenResult(account)
    }

    @Transactional
    fun loginUserPerToken(token: String): LoginResult {
        val invitation = loginTokenRepository.findByToken(token)
            ?: throw UnauthorizedException("Token is not found")
        val person = personRepository.findByIdOrNull(invitation.personId)
            ?: throw UnauthorizedException("Token is not valid")
        val account = accountRepository.findByIdOrNull(person.accountId)
            ?: throw UnauthorizedException("Token is not usable")
        return generateTokenResult(account)
    }

    private fun generateTokenResult(account: Account): LoginResult {
        val refreshToken = jwtService.generateRefreshToken(account.id, account.email)
        val accessToken =
            jwtService.generateAccessToken(account.id, account.email, personsIds = account.persons.map { it.id })
        accountRepository.save(account.copy(lastLogin = ZonedDateTime.now()))
        return LoginResult(
            account = account,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    @Transactional
    fun registerAccount(
        @Email email: String,
        @NotBlank plainPassword: String,
        @NotBlank name: String,
        optionalGroupId: UUID? = null,
    ): Account {
        val account = createAccount(email, plainPassword)
        val person = createInitialGroupPerson(name, account.id, optionalGroupId)
        val finalAccount = accountRepository.save(account.copy(persons = listOf(person)))
        return accountRepository.findOneByEmail(finalAccount.email)!!
    }

    @Transactional
    fun registerAnalogueAccount(
        creator: Account,
        @NotBlank name: String,
        neededGroupId: UUID,
    ): Account {
        var freeToken: String
        // generate an unused LoginToken token
        while (true) {
            freeToken = nameGeneratorService.generateName()
            loginTokenRepository.findByToken(freeToken) ?: break
        }

        val account = createAnalogueAccount(creator, freeToken)
        val group = groupRepository.findByIdOrNull(neededGroupId)
            ?: throw SecurityError.GroupNotFound(neededGroupId.toString())
        val person = createInitialGroupPerson(name, account.id, neededGroupId, type = MembershipType.ANALOGUE)
        val finalAccount = accountRepository.save(account.copy(persons = listOf(person)))
        loginTokenRepository.save(LoginToken(randomUUID(), person.id, freeToken))
        groupRepository.save(group.copy(name = person.name))
        return finalAccount
    }

    /**
     * Can create a Registration for a new User with an invitation (optionalGroupId), or
     * create an implicit Group
     */
    private fun createInitialGroupPerson(
        name: String,
        accountId: UUID,
        optionalGroupId: UUID? = null,
        type: MembershipType = MembershipType.MEMBER
    ): Person {
        StringValidator.validateLength(name, 6)
        var memberType = type
        val chosenGroupId = if (optionalGroupId != null) {
            groupRepository.findByIdOrNull(optionalGroupId)?.id
                ?: throw SecurityError.GroupNotFound(optionalGroupId.toString())
        } else {
            memberType = MembershipType.OWNER
            createImplicitGroup(accountId, optionalGroupId).id
        }

        return Person(
            id = randomUUID(),
            accountId = accountId,
            name = name,
            groupId = chosenGroupId,
            memberType = memberType
        )

    }


    @Deprecated("Needs to deprecated")
    private fun createImplicitGroup(accountId: UUID, optionalGroupId: UUID?): Group {
        val group = groupRepository.findByName(accountId.toString()).firstOrNull()
        if (group != null) {
            throw SecurityError.GroupCannotBeCreated(optionalGroupId.toString())
        }
        val newGroupId = randomUUID()
        return groupRepository.save(
            Group(
                newGroupId,
                accountId.toString(),
                listOf()
            )
        )
    }

    @Transactional
    fun refreshAccessToken(refreshToken: String): TokenResult {

        val claims = try {
            jwtService.validateRefreshToken(refreshToken)
            jwtService.getRefreshClaims(refreshToken)
        } catch (e: Exception) {
            throw UnauthorizedException(e.message)
        }

        log.info("RefreshToken for user ${claims.subject} was jwt valid")

        val account = accountRepository.findOneByEmail(claims.subject)
            ?: throw UnauthorizedException(claims.subject)

        val lastRevocation = account.lastRevocationDate ?: ZonedDateTime.now().minusYears(5)

        log.info("RefreshToken issuedAt ${claims.issuedAt}")
        if (lastRevocation.toInstant().isAfter(claims.issuedAt.toInstant())) {
            throw UsernameNotFoundException("Token expired or revoked. Please login again")
        }

        val personsIds = account.persons.map { it.id }

        accountRepository.save(account.copy(lastRefresh = ZonedDateTime.now()))
        return jwtService.generateAccessToken(account.id, account.email, personsIds = personsIds)
    }

    @Transactional
    fun checkValidAccessToken(accessToken: String): UserDetails = try {
        val claims = jwtService.getAccessClaims(accessToken)
        jwtService.validateAccessToken(accessToken)

        val tokenUserDetails = TokenUserDetails(
            accountId = UUID.fromString(claims[JwtService.CLAIM_ACCOUNT_ID].toString()),
            email = claims.subject,
            personsIds = (claims[JwtService.CLAIM_PERSONS_IDS] as List<*>).map { UUID.fromString(it.toString()) },
            expiration = claims.expiration,
            issuedAt = claims.issuedAt,
            issuer = claims.issuer,
        )
        tokenUserDetails
    } catch (e: Exception) {
        throw UnauthorizedException(e.message)
    }

    @Transactional
    fun createAccount(email: String, plainPassword: String): Account {
        log.info("Someone tries to register: $email")

        try {
            if (!email.endsWith(AMIGO_ANALOGUE_EMAIL_SUFFIX)) EmailValidator.validate(email)
        } catch (e: Exception) {
            throw AccountCreationError.EmailInvalid(email)
        }
        try {
            PasswordValidator.validate(plainPassword)
            StringValidator.validateLength(plainPassword, 6)
        } catch (e: Exception) {
            throw AccountCreationError.PasswordInvalid()
        }

        accountRepository.findOneByEmail(email)?.let {
            throw AccountCreationError.EmailUsed(email)
        }
        val passwordEncrypted = passwordEncoder.encode(plainPassword)
        val id = randomUUID()
        val account = Account(
            id = id,
            email = email,
            passwordEncrypted = passwordEncrypted,
        )
        return accountRepository.save(account)
    }

    @Transactional
    fun createAnalogueAccount(creator: Account, password: String): Account {
        val timestamp = System.currentTimeMillis()
        val email = "user-$timestamp$AMIGO_ANALOGUE_EMAIL_SUFFIX"
        val account = createAccount(email, password).copy(
            createdByAccountId = creator.id,
            hasValidMail = false,
        )
        return accountRepository.save(account)
    }

    @Transactional
    fun requestPasswordChange(account: Account): Account {
        return accountRepository.save(
            account.copy(
                changeAccountToken = randomUUID().toString(),
                changeAccountTokenCreatedAt = ZonedDateTime.now()
            )
        )
    }

    @Transactional
    fun changePasswordForUser(account: Account, newPassword: String): Boolean {
        val passwordEncrypted = passwordEncoder.encode(newPassword)
        accountRepository.save(account.copy(passwordEncrypted = passwordEncrypted))
        return true
    }

    @Transactional
    fun setFcmToken(accountId: UUID, fcmToken: String): Account {
        val account = accountRepository.findByIdOrNull(accountId)
            ?: throw UnauthorizedException("Cannot update fcm token without account")
        return accountRepository.save(account.copy(fcmToken = fcmToken)).also {
            log.info("Set FCM token for account ${accountId} : $fcmToken")
        }
    }

    fun findById(id: UUID) = accountRepository.findByIdOrNull(id)
    fun findOneByEmail(email: String) = accountRepository.findOneByEmail(email)

    fun findOneByPersonId(id: UUID) = personRepository.findByIdOrNull(id)?.let {
        accountRepository.findByIdOrNull(it.accountId)
    }

    fun count(): Long = accountRepository.count()
    fun findByIdOrNull(id: UUID): Account? = accountRepository.findByIdOrNull(id)

}

sealed class AccountCreationError(errorName: String, message: String, cause: Throwable? = null) :
    ServiceError(errorName, message, cause) {
    class EmailUsed(name: String) : AccountCreationError("ACCOUNT_CREATE_EMAIL_USED", "Email is already used: $name")
    class EmailInvalid(name: String) : AccountCreationError("ACCOUNT_CREATE_EMAIL_INVALID", "Email is invalid: $name")
    class PasswordInvalid : AccountCreationError("ACCOUNT_CREATE_PASSWORD_INVALID", "Password is invalid")
}

