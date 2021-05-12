package org.ossiaustria.amigo.platform.domain.services.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.ValidationException
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.services.AbstractServiceTest
import org.ossiaustria.amigo.platform.exceptions.UnauthorizedException
import org.ossiaustria.amigo.platform.exceptions.UserAlreadyExistsException
import org.springframework.beans.factory.annotation.Autowired
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
    fun `registerUser should create a person for the new account`() {
        val result: Account = service.registerUser("user@example.org", "password", "Username")
        assertThat(result).isNotNull
        assertThat(result.person()).isNotNull
        assertThat(result.person().name).isNotNull
        assertThat(result.person().name).isEqualTo("Username")
    }
}