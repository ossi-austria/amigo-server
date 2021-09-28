package org.ossiaustria.amigo.platform.domain.services.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.services.AbstractServiceTest
import org.ossiaustria.amigo.platform.domain.services.ServiceError
import org.ossiaustria.amigo.platform.exceptions.UnauthorizedException
import org.ossiaustria.amigo.platform.exceptions.UserAlreadyExistsException
import org.ossiaustria.amigo.platform.exceptions.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID.randomUUID

internal class AuthServiceTest : AbstractServiceTest() {

    @Autowired
    private lateinit var service: AuthService

    @Autowired //@MockkBean(relaxed = true, relaxUnitFun = true)
    private lateinit var accountRepository: AccountRepository

    private val passwordEncoder = BCryptPasswordEncoder()

    @BeforeEach
    fun beforeEach() {
        cleanTables()
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
        assertThrows<ValidationException> {
            service.registerUser("", "password", "Username")
        }
    }

    @Test
    fun `registerUser should throw when password is empty`() {
        assertThrows<ValidationException> {
            service.registerUser("user@example.org", "", "Username")
        }
    }

    @Test
    fun `registerUser should throw when name is empty`() {
        assertThrows<ValidationException> {
            service.registerUser("user@example.org", "password", "")
        }
    }

    @Test
    fun `registerUser should throw when user already exists`() {
        accountRepository.save(
            Account(randomUUID(), "user@example.org", passwordEncoder.encode("password"))
        )
        assertThrows<UserAlreadyExistsException> {
            service.registerUser("user@example.org", "password", "Username")
        }
    }

    @Test
    fun `registerUser should succeed when email is not used`() {
        val result: Account = service.registerUser("user@example.org", "password", "Username")
        assertThat(result).isNotNull
        assertThat(result.lastLogin).isNull()
        assertThat(result.email).isEqualTo("user@example.org")
    }

    @Test
    fun `registerUser should create a person for the new account with implicitGroup`() {
        val result: Account = service.registerUser(
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
            service.registerUser(
                "user@example.org", "password", "Username", optionalGroupId = randomUUID()
            )
        }
    }

    @Test
    fun `registerUser should use provided valid optionalGroupId`() {
        val group = groups.save(Group(randomUUID(), "existing"))
        val result: Account = service.registerUser(
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
}
