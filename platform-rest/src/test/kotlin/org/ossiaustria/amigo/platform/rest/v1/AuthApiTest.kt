package org.ossiaustria.amigo.platform.rest.v1

import org.ossiaustria.amigo.platform.utils.RandomUtils
import com.ninjasquad.springmockk.SpykBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.rest.v1.auth.*
import org.ossiaustria.amigo.platform.services.auth.TokenDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.annotation.Rollback
import java.time.ZonedDateTime
import java.util.UUID
import javax.mail.internet.MimeMessage
import javax.transaction.Transactional

class AuthApiTest : AbstractRestApiTest() {

    val authUrl = "/api/v1/auth"

    @SpykBean
    lateinit var mailSender: JavaMailSender

    @Autowired
    private lateinit var accountSubjectPreparationTrait: AccountSubjectPreparationTrait

    @BeforeEach
    @AfterEach
    fun clearRepo() {
        truncateAllTables()

        every { mailSender.send(ofType(SimpleMailMessage::class)) } just Runs
        every { mailSender.send(ofType(MimeMessage::class)) } just Runs

        accountSubjectPreparationTrait.apply()

        account = accountSubjectPreparationTrait.account
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `Can register with new user`() {
        val randomUserName = RandomUtils.generateRandomUserName(10)
        val randomPassword = RandomUtils.generateRandomPassword(30, true)
        val email = "$randomUserName@example.com"
        val registerRequest = RegisterRequest(randomUserName, email, randomPassword, "absolute-new-name")

        val url = "$authUrl/register"

        val result = this.performPost(url, body = registerRequest)
            .expectOk()
            .document(
                "register-success",
                requestFields(registerRequestFields()),
                responseFields(userSecretDtoResponseFields())
            )
            .returns(SecretAccountDto::class.java)

        with(accountRepository.findOneByEmail(email)!!) {
            assertThat(id).isEqualTo(result.id)
        }
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `Cannot register with existing user`() {
        val existingUser = createMockUser()
        val registerRequest =
            RegisterRequest(existingUser.email, existingUser.email, "any other password", "another-new-name")

        val url = "$authUrl/register"

        this.performPost(url, body = registerRequest)
            .expect4xx()
            .document("register-fail",
                responseFields(errorResponseFields()))
    }


    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `Cannot login with Gitlab is rejected credentials`() {

        val plainPassword = "password"
        val existingUser = createMockUser(plainPassword, "0000")
        val loginRequest = LoginRequest(existingUser.email, existingUser.email, plainPassword)

        val url = "$authUrl/login"

        this.performPost(url, body = loginRequest)
            .expect4xx()
            .document("login-fail",
                responseFields(errorResponseFields()))
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `Can update an existing user`() {
        val randomUserName = RandomUtils.generateRandomUserName(10)
        val randomPassword = RandomUtils.generateRandomPassword(30, true)
        val email = "$randomUserName@example.com"
        val registerRequest = RegisterRequest(randomUserName, email, randomPassword, "absolute-new-name")

        val url = "$authUrl/register"

        val result = this.performPost(url, body = registerRequest)
            .expectOk()
            .returns(SecretAccountDto::class.java)

        with(accountRepository.findOneByEmail(email)!!) {
            assertThat(id).isEqualTo(result.id)
            assertThat(email).isEqualTo(result.email).isEqualTo(randomUserName)
        }

        val newRandomUserName = RandomUtils.generateRandomUserName(10)
        val newEmail = "$newRandomUserName@example.com"

        val updateRequest = UpdateRequest(
            newRandomUserName,
            newEmail
        )

        val tokenDetails = TokenDetails(
            result.email,
            result.id,
            UUID.randomUUID(),
            result.email,
        )

        mockSecurityContextHolder(tokenDetails)

        val returnedResult2: AccountDto = this.performPut(
            "$authUrl/update/${result.id}",
            token = "new-token-${UUID.randomUUID()}",
            body = updateRequest
        )
            .expectOk()
            .document(
                "update-profile-success",
                requestFields(updateProfileRequestFields()),
                responseFields(AccountDtoResponseFields())
            )
            .returns()

        with(accountRepository.findOneByEmail(newEmail)!!) {
            assertThat(id).isEqualTo(returnedResult2.id)
            assertThat(email).isEqualTo(returnedResult2.email).isEqualTo(newRandomUserName)
        }

        assertThat(accountRepository.findOneByEmail(email)).isNull()
        assertThat(accountRepository.findOneByEmail(randomUserName)).isNull()
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `Can update own user`() {
        val randomUserName = RandomUtils.generateRandomUserName(10)
        val randomPassword = RandomUtils.generateRandomPassword(30, true)
        val email = "$randomUserName@example.com"
        val registerRequest = RegisterRequest(randomUserName, email, randomPassword, "absolute-new-name")

        val result = this.performPost("$authUrl/register", body = registerRequest)
            .expectOk()
            .returns(SecretAccountDto::class.java)

        mockSecurityContextHolder(
            TokenDetails(
                result.email,
                result.id,
                UUID.randomUUID(),
                "new-token-${UUID.randomUUID()}",
            )
        )

        val returnedResult2: AccountDto = this.performPut(
            "$authUrl/user",
            token = "new-token-${UUID.randomUUID()}",
            body = UpdateRequest(
                termsAcceptedAt = ZonedDateTime.now(),
                hasNewsletters = true,
                email = email,
            )
        )
            .expectOk()
            .document(
                "update-own-profile-success",
                requestFields(updateProfileRequestFields()),
                responseFields(AccountDtoResponseFields())
            )
            .returns()

        with(accountRepository.findOneByEmail(email)!!) {
            assertThat(id).isEqualTo(returnedResult2.id)
        }
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `Can get who-am-i`() {

        mockUserAuthentication()

        val result: AccountDto = this.performGet("$authUrl/whoami", token = "new-token-${UUID.randomUUID()}")
            .expectOk()
            .document(
                "who-am-i",
                responseFields(AccountDtoResponseFields())
            )
            .returns()

        assertThat(account.id).isEqualTo(result.id)
        assertThat(account.email).isEqualTo(result.email)
        assertThat(account.email).isEqualTo(result.email)
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `Can check token`() {
        mockUserAuthentication()

        val result: AccountDto = this.performGet("$authUrl/check/token", token = "new-token-${UUID.randomUUID()}")
            .expectOk()
            .document(
                "check-token",
                responseFields(AccountDtoResponseFields())
            )
            .returns()

        assertThat(account.id).isEqualTo(result.id)
        assertThat(result.email).isEqualTo("mock_user")
        assertThat(result.email).isEqualTo("mock@example.com")
    }

    private fun userSecretDtoResponseFields(): List<FieldDescriptor> {
        return listOf(
            fieldWithPath("id").type(JsonFieldType.STRING).description("UUID"),
            fieldWithPath("email").type(JsonFieldType.STRING).description("An unique email"),
            fieldWithPath("email").type(JsonFieldType.STRING).description("An valid email"),
            fieldWithPath("gitlab_id").type(JsonFieldType.NUMBER).description("A gitlab id"),
            fieldWithPath("token").type(JsonFieldType.STRING).optional().description(""),
            fieldWithPath("access_token").type(JsonFieldType.STRING).optional().description(""),
            fieldWithPath("refresh_token").type(JsonFieldType.STRING).optional().description(""),
            fieldWithPath("user_role").optional().type(JsonFieldType.STRING)
                .description("UserRole describes the main usage type of this user"),
            fieldWithPath("terms_accepted_at").optional().type(JsonFieldType.STRING)
                .description("Timestamp, when the terms & conditions have been accepted."),
            fieldWithPath("has_newsletters").optional().type(JsonFieldType.BOOLEAN)
                .description("Indicates that the user wants to retrieve newsletters, or not"),
        )
    }

    private fun AccountDtoResponseFields(): List<FieldDescriptor> {
        return listOf(
            fieldWithPath("id").type(JsonFieldType.STRING).description("UUID"),
            fieldWithPath("email").type(JsonFieldType.STRING).description("An unique email"),
            fieldWithPath("email").type(JsonFieldType.STRING).description("An valid email"),
            fieldWithPath("gitlab_id").type(JsonFieldType.NUMBER).description("A gitlab id"),
            fieldWithPath("user_role").optional().type(JsonFieldType.STRING)
                .description("UserRole describes the main usage type of this user"),
            fieldWithPath("terms_accepted_at").optional().type(JsonFieldType.STRING)
                .description("Timestamp, when the terms & conditions have been accepted."),
            fieldWithPath("has_newsletters").optional().type(JsonFieldType.BOOLEAN)
                .description("Indicates that the user wants to retrieve newsletters, or not"),
        )
    }

    private fun registerRequestFields(): List<FieldDescriptor> {
        return listOf(
            fieldWithPath("password").type(JsonFieldType.STRING).description("A plain text password"),
            fieldWithPath("email").type(JsonFieldType.STRING).description("A valid, not-yet-existing email"),
            fieldWithPath("email").type(JsonFieldType.STRING).description("A valid email"),
            fieldWithPath("name").type(JsonFieldType.STRING).description("The fullname of the user"),
        )
    }

    private fun updateProfileRequestFields(): List<FieldDescriptor> {
        return listOf(
            fieldWithPath("email").type(JsonFieldType.STRING).description("A valid, not-yet-existing email"),
            fieldWithPath("email").type(JsonFieldType.STRING).description("A valid email"),
            fieldWithPath("name").type(JsonFieldType.STRING).optional().description("The fullname of the user"),
            fieldWithPath("user_role").optional().type(JsonFieldType.STRING).description(
                "UserRole: Can be DATA_SCIENTIST,\n" +
                        "    DEVELOPER,\n" +
                        "    ML_ENGINEER,\n" +
                        "    RESEARCHER,\n" +
                        "    STUDENT,\n" +
                        "    TEAM_LEAD,"
            ),
            fieldWithPath("terms_accepted_at").optional().type(JsonFieldType.STRING)
                .description("Timestamp, when the terms & conditions have been accepted."),
            fieldWithPath("has_newsletters").optional().type(JsonFieldType.BOOLEAN)
                .description("Indicates that the user wants to retrieve newsletters, or not")

        )
    }

    private fun loginRequestFields(): List<FieldDescriptor> {
        return listOf(
            fieldWithPath("password").type(JsonFieldType.STRING).description("The plain text password"),
            fieldWithPath("email").type(JsonFieldType.STRING).optional()
                .description("At least email or email has to be provided"),
            fieldWithPath("email").type(JsonFieldType.STRING).optional()
                .description("At least email or email has to be provided")
        )
    }
}
