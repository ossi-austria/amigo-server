package org.ossiaustria.amigo.platform.domain.services.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.LoginToken
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.repositories.LoginTokenRepository
import org.ossiaustria.amigo.platform.domain.services.AbstractServiceTest
import org.ossiaustria.amigo.platform.domain.services.ServiceError
import org.ossiaustria.amigo.platform.exceptions.UnauthorizedException
import org.ossiaustria.amigo.platform.exceptions.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.ZonedDateTime
import java.util.UUID.randomUUID

internal class AuthServiceTest : AbstractServiceTest() {

    @Autowired
    private lateinit var service: AuthService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var loginTokenRepository: LoginTokenRepository

    private val passwordEncoder = BCryptPasswordEncoder()

    @BeforeEach
    fun beforeEach() {
        cleanTables()
        mockPersons()
    }

    @Test
    fun `login should throw when user is not found`() {
        assertThrows<UnauthorizedException> {
            service.loginUser("user@example.org", "password")
        }
    }

    @Test
    fun `login should throw when wrong password is used`() {
        assertThrows<UnauthorizedException> {
            service.loginUser("user@example.org", "password")
        }
    }

    @Test
    fun `login should succeed for existing user with correct password`() {
        val id = randomUUID()
        accountRepository.save(
            Account(id, "user@example.org", passwordEncoder.encode("password"))
        )
        val result: LoginResult = service.loginUser("user@example.org", "password")
        assertThat(result).isNotNull
        assertThat(result.account.id).isEqualTo(id)
        assertThat(result.accessToken).isNotNull
        assertThat(result.refreshToken).isNotNull
        assertThat(result.account.lastLogin).isNotNull
        assertThat(result.account.email).isNotNull
    }

    @Test
    fun `loginPerToken should succeed for existing Person with person`() {
        val id = randomUUID()
        loginTokenRepository.save(
            LoginToken(id, personId1, "token-person-1")
        )
        val result: LoginResult = service.loginUserPerToken("token-person-1")
        assertThat(result).isNotNull
        assertThat(result.account.id).isEqualTo(accounts1.id)
        assertThat(result.accessToken).isNotNull
        assertThat(result.refreshToken).isNotNull
        assertThat(result.account.lastLogin).isNotNull
        assertThat(result.account.email).isNotNull
    }

    @Test
    fun `loginPerToken must fail when no Person exist for that token`() {
        assertThrows<UnauthorizedException> {
            service.loginUserPerToken("token-person-1")
        }
    }

    @Test
    fun `login should succeed multiple times`() {
        val id = randomUUID()
        accountRepository.save(
            Account(id, "user@example.org", passwordEncoder.encode("password"))
        )

        service.loginUser("user@example.org", "password")
        val result: LoginResult = service.loginUser("user@example.org", "password")

        assertThat(result).isNotNull
        assertThat(result.account.id).isEqualTo(id)
        assertThat(result.accessToken).isNotNull
        assertThat(result.refreshToken).isNotNull
        assertThat(result.account.lastLogin).isNotNull
        assertThat(result.account.email).isNotNull
    }

    @Test
    fun `registerUser should throw when email is empty`() {
        assertThrows<AccountCreationError.EmailInvalid> {
            service.registerAccount("", "password", "Username")
        }
    }

    @Test
    fun `registerUser should throw when password is empty`() {
        assertThrows<AccountCreationError.PasswordInvalid> {
            service.registerAccount("user@example.org", "", "Username")
        }
    }

    @Test
    fun `registerUser should throw when name is empty`() {
        assertThrows<ValidationException> {
            service.registerAccount("user@example.org", "password", "")
        }
    }

    @Test
    fun `registerUser should throw when user already exists`() {
        accountRepository.save(
            Account(randomUUID(), "user@example.org", passwordEncoder.encode("password"))
        )
        assertThrows<AccountCreationError.EmailUsed> {
            service.registerAccount("user@example.org", "password", "Username")
        }
    }

    @Test
    fun `registerUser should succeed when email is not used`() {
        val result: Account = service.registerAccount("user@example.org", "password", "Username")
        assertThat(result).isNotNull
        assertThat(result.lastLogin).isNull()
        assertThat(result.email).isEqualTo("user@example.org")
    }

    @Test
    fun `registerUser should create a person for the new account with implicitGroup`() {
        val result: Account = service.registerAccount(
            "user@example.org", "password", "Username"
        )
        assertThat(result).isNotNull
        assertThat(result.persons).isNotEmpty
        val groupId = result.persons.first().groupId
        val group = groups.findByIdOrNull(groupId)
        assertThat(group).isNotNull
        assertThat(group!!.name).isEqualTo(result.id.toString())
    }

    @Test
    fun `registerUser must not succeed with a invalid optionalGroupId`() {

        assertThrows<ServiceError> {
            service.registerAccount(
                "user@example.org", "password", "Username", optionalGroupId = randomUUID()
            )
        }
    }

    @Test
    fun `registerUser should use provided valid optionalGroupId`() {
        val group = groups.save(Group(randomUUID(), "existing"))
        val result: Account = service.registerAccount(
            "user@example.org", "password", "Username",
            optionalGroupId = group.id
        )
        assertThat(result).isNotNull
        assertThat(result.persons).isNotEmpty
        val first = result.persons.first()
        assertThat(first).isNotNull
        assertThat(first.groupId).isEqualTo(group.id)
        val groupAfterUpdate = groups.findByIdOrNull(first.groupId)
        assertThat(groupAfterUpdate).isNotNull
        assertThat(groupAfterUpdate!!.name).isNotEqualTo(result.id)
        assertThat(groupAfterUpdate.name).isEqualTo(group.name)
        assertThat(groupAfterUpdate.members.map { it.id }).contains(first.id)
    }

    @Test
    fun `registerAnalogueUser should create new Account`() {
        val result: Account = service.registerAnalogueAccount(accounts1, "Firstname lastname", person1.groupId)
        assertThat(result).isNotNull
        assertThat(result.lastLogin).isNull()
        assertThat(result.email).endsWith("@amigobox")
        assertThat(result.passwordEncrypted).isNotBlank
    }

    @Test
    fun `registerAnalogueUser should create new Person in matching Group`() {
        val result: Account = service.registerAnalogueAccount(accounts1, "Firstname lastname", person1.groupId)
        val person = result.persons.first()
        assertThat(person).isNotNull
        assertThat(person.name).isEqualTo("Firstname lastname")
        assertThat(person.memberType).isEqualTo(MembershipType.ANALOGUE)
        assertThat(person.groupId).isEqualTo(person1.groupId)
    }

    @Test
    fun `registerAnalogueUser should rename the Group to Analogue's name`() {
        val result: Account = service.registerAnalogueAccount(accounts1, "Firstname lastname", person1.groupId)
        val groupId = result.persons.first().groupId
        val group = groups.findByIdOrNull(groupId)
        assertThat(group).isNotNull
        assertThat(group!!.name).contains("Firstname lastname")
    }

    @Test
    fun `registerAnalogueUser should create LoginToken for new Person`() {
        val account: Account = service.registerAnalogueAccount(accounts1, "Firstname lastname", person1.groupId)
        val person = account.persons.first()

        val result = loginTokenRepository.findByPersonId(person.id)
        assertThat(result).isNotNull
        assertThat(result!!.token).isNotNull
    }

    @Test
    fun `registerAnalogueUser should set creatorId`() {
        val account: Account = service.registerAnalogueAccount(accounts1, "Firstname lastname", person1.groupId)
        assertThat(account).isNotNull
        assertThat(account.createdByAccountId).isEqualTo(accounts1.id)
    }

    @Test
    fun `createAccount should create a new account`() {
        val result = service.createAccount("newaccount@example.org", "password")
        assertThat(result).isNotNull
        assertThat(result.email).isEqualTo("newaccount@example.org")
        assertThat(result.passwordEncrypted).isNotBlank
        assertThat(result.passwordEncrypted).isNotEqualTo("password")
    }

    @Test
    fun `createAccount must not create an Analogue-only account as default`() {
        val result = service.createAccount("newaccount@example.org", "password")
        assertThat(result).isNotNull
        assertThat(result.createdByAccountId).isNull()
        assertThat(result.hasValidMail).isTrue
    }

    @Test
    fun `createAccount must not save a invalid email address`() {
        assertThrows<AccountCreationError.EmailInvalid> {
            service.createAccount("newaccount", "password")
        }
    }

    @Test
    fun `createAccount must not save a already used email address`() {
        assertThrows<AccountCreationError.EmailUsed> {
            service.createAccount(accounts1.email, "password")
        }
    }

    @Test
    fun `createAccount must not save a invalid password`() {
        assertThrows<AccountCreationError.PasswordInvalid> {
            service.createAccount("newaccount@example.org", "a b")
        }
    }

    @Test
    fun `createAnalogueAccount should create an Analogue-only account`() {
        val result = service.createAnalogueAccount(accounts1, "password")
        assertThat(result).isNotNull
        assertThat(result.email).endsWith("@amigobox")
        assertThat(result.passwordEncrypted).isNotBlank
        assertThat(result.passwordEncrypted).isNotEqualTo("password")
        assertThat(result.createdByAccountId).isNotNull
        assertThat(result.hasValidMail).isFalse
    }

    @Test
    fun `requestPasswordChange should set changeAccountToken`() {
        val result = service.requestPasswordChange(accounts1)
        assertThat(result).isNotNull
        assertThat(result.email).isEqualTo(accounts1.email)
        assertThat(result.changeAccountToken).isNotBlank
        assertThat(result.changeAccountTokenCreatedAt).isAfter(ZonedDateTime.now().minusHours(1))
        assertThat(result.changeAccountTokenCreatedAt).isBefore(ZonedDateTime.now().plusHours(1))
        assertThat(result.passwordEncrypted).isEqualTo(accounts1.passwordEncrypted)
    }
}

