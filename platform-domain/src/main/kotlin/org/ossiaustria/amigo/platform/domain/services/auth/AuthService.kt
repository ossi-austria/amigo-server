package org.ossiaustria.amigo.platform.domain.services.auth


import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.EmailValidator
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.StringValidator
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.repositories.GroupRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.SecurityError
import org.ossiaustria.amigo.platform.exceptions.UnauthorizedException
import org.ossiaustria.amigo.platform.exceptions.UserAlreadyExistsException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.validation.Validator
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
    private lateinit var validator: Validator

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)

        val GUEST_ACCOUNT_ID = UUID(0L, 0L)

        private const val DEFAULT_GROUP = "AMIGO"
    }

    @Transactional
    fun loginUser(email: String, plainPassword: String): LoginResult {
        log.info("User tries to login: $email")
        val account: Account = accountRepository.findOneByEmail(email)
            ?: throw UnauthorizedException("username or password is incorrect")

        val checkPw = passwordEncoder.matches(plainPassword, account.passwordEncrypted)

        if (!checkPw) throw UnauthorizedException("username or password is incorrect")

        val refreshToken = jwtService.generateRefreshToken(account.id, email)
        val accessToken = jwtService.generateAccessToken(account.id, email, personsIds = account.persons.map { it.id })
        accountRepository.save(account.copy(lastLogin = ZonedDateTime.now()))
        return LoginResult(
            account = account,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    @Transactional
    fun registerUser(
        @Email email: String,
        @NotBlank plainPassword: String,
        @NotBlank name: String,
        optionalGroupId: UUID? = null,
    ): Account {

        EmailValidator.validate(email)
        StringValidator.validateLength(plainPassword, 6)
        StringValidator.validateLength(name, 6)
        val encryptedPassword = passwordEncoder.encode(plainPassword)
        val byEmail: Account? = accountRepository.findOneByEmail(email)

        log.info("Someone tries to register: $name $email")
        if (byEmail != null) throw UserAlreadyExistsException(email, email)

        val accountId = randomUUID()

        val initialPersons = createInitialGroupPersons(name, accountId, optionalGroupId)
        val account = Account(
                id = accountId, email = email, passwordEncrypted = encryptedPassword,
                persons = initialPersons
            )

        val newUser = accountRepository.save(account)

        return accountRepository.findOneByEmail(newUser.email)!!
    }

    /**
     * Can create a Registration for a new User with an invitation (optionalGroupId), or
     * create an implicit Group
     */
    fun createInitialGroupPersons(
        name: String,
        accountId: UUID,
        optionalGroupId: UUID? = null,
    ): List<Person> {
        var type = MembershipType.MEMBER
        val chosenGroupId = if (optionalGroupId != null) {
            groupRepository.findByIdOrNull(optionalGroupId)?.id
                ?: throw SecurityError.GroupNotFound(optionalGroupId.toString())
        } else {
            type = MembershipType.OWNER
            createImplicitGroup(accountId, optionalGroupId).id
        }

        return listOf(
            Person(
                id = randomUUID(),
                accountId = accountId,
                name = name,
                groupId = chosenGroupId,
                memberType = type
            )
        )
    }

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
}

