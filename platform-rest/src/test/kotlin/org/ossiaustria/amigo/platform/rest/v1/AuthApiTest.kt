package org.ossiaustria.amigo.platform.rest.v1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.rest.v1.user.AccountDto
import org.ossiaustria.amigo.platform.rest.v1.user.LoginRequest
import org.ossiaustria.amigo.platform.rest.v1.user.LoginResultDto
import org.ossiaustria.amigo.platform.rest.v1.user.RefreshAccessTokenRequest
import org.ossiaustria.amigo.platform.rest.v1.user.RegisterRequest
import org.ossiaustria.amigo.platform.rest.v1.user.SecretAccountDto
import org.ossiaustria.amigo.platform.rest.v1.user.TokenResultDto
import org.ossiaustria.amigo.platform.utils.RandomUtils
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType.ARRAY
import org.springframework.restdocs.payload.JsonFieldType.OBJECT
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import java.util.UUID

internal class AuthApiTest : AbstractRestApiTest() {

    val authUrl = "/v1/auth"

    @BeforeEach
    fun prepare() {
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `should register with new user`() {
        val randomUserName = RandomUtils.generateRandomUserName(10)
        val randomPassword = RandomUtils.generateRandomPassword(30, true)
        val email = "$randomUserName@example.com"
        val registerRequest = RegisterRequest(email, randomPassword, "absolute-new-name")

        val url = "$authUrl/register"

        val result = this.performPost(url, body = registerRequest)
            .expectOk()
            .document(
                "register-success",
                requestFields(registerRequestFields()),
                responseFields(userSecretDtoResponseFields())
            ).returns(SecretAccountDto::class.java)

        with(accountService.findOneByEmail(email)!!) {
            assertThat(id).isEqualTo(result.id)
        }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `should register with new user for explicit Group`() {
        val randomUserName = RandomUtils.generateRandomUserName(10)
        val randomPassword = RandomUtils.generateRandomPassword(30, true)
        val email = "$randomUserName@example.com"
        val registerRequest = RegisterRequest(
            email, randomPassword, "absolute-new-name", optionalGroupId = group.id
        )

        val url = "$authUrl/register"

        val result = this.performPost(url, body = registerRequest)
            .expectOk()
            .document(
                "register-explicit-success",
                requestFields(registerRequestFields()),
                responseFields(userSecretDtoResponseFields())
            ).returns(SecretAccountDto::class.java)

        with(accountService.findOneByEmail(email)!!) {
            assertThat(id).isEqualTo(result.id)
        }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `must not register with existing user`() {
        val existingUser = createMockUser("password", "0001")
        val registerRequest =
            RegisterRequest(existingUser.email, "any other password", "another-new-name")

        val url = "$authUrl/register"

        this.performPost(url, body = registerRequest)
            .expect4xx()
            .document("register-fail", responseFields(errorResponseFields()))
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `should login with correct credentials`() {

        val existingUser = createMockUser("password", "0001")
        val loginRequest = LoginRequest(existingUser.email, "password")

        val url = "$authUrl/login"

        val loginResult = this.performPost(url, body = loginRequest)
            .expectOk()
            .document(
                "login-success",
                requestFields(loginRequestFields()),
                responseFields(loginResponseFields())
            ).returns(LoginResultDto::class.java)

        this.performGet(securedUrl(loginResult.account.persons.first().id), accessToken = loginResult.accessToken.token)
            .expectOk()

    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `login should return usable accessToken`() {

        val existingUser = createMockUser("password", "0001")
        val loginRequest = LoginRequest(existingUser.email, "password")

        val loginResult = this.performPost("$authUrl/login", body = loginRequest).returns(LoginResultDto::class.java)

        this.performGet(securedUrl(loginResult.account.persons.first().id), accessToken = loginResult.accessToken.token)
            .expectOk()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `login should return usable refreshToken`() {

        val existingUser = createMockUser("password", "0001")
        val loginRequest = LoginRequest(existingUser.email, "password")

        val loginResult = this.performPost("$authUrl/login", body = loginRequest).returns(LoginResultDto::class.java)

        RefreshAccessTokenRequest(refreshToken = loginResult.refreshToken.token).let {
            this.performPost("$authUrl/refresh-token", body = it).expectOk()
        }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `login should return accessToken invalid for refresh`() {

        val existingUser = createMockUser("password", "0001")
        val loginRequest = LoginRequest(existingUser.email, "password")

        val loginResult = this.performPost("$authUrl/login", body = loginRequest).returns(LoginResultDto::class.java)

        RefreshAccessTokenRequest(refreshToken = loginResult.accessToken.token).let {
            this.performPost("$authUrl/refresh-token", body = it).expectUnauthorized()
        }
    }


    @Test
    @Tag(TestTags.RESTDOC)
    fun `login should return refreshToken invalid for access`() {

        val existingUser = createMockUser("password", "0001")
        val loginRequest = LoginRequest(existingUser.email, "password")

        val loginResult = this.performPost("$authUrl/login", body = loginRequest).returns(LoginResultDto::class.java)

        this.performGet(securedUrl(), accessToken = loginResult.refreshToken.token).expectUnauthorized()
    }


    @Test
    @Tag(TestTags.RESTDOC)
    fun `must not login with wrong credentials`() {

        val existingUser = createMockUser("password", "0001")
        val loginRequest = LoginRequest(existingUser.email, "wrongpassword")

        val url = "$authUrl/login"

        this.performPost(url, body = loginRequest)
            .expect4xx()
            .document("login-fail", responseFields(errorResponseFields()))
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `should return new accessToken with valid refreshToken`() {

        mockUserAuthentication(refreshTokenTime = 8)

        val accessToken = this.performPost(
            "$authUrl/refresh-token",
            body = RefreshAccessTokenRequest(refreshToken = refreshToken.token)
        )
            .expectOk()
            .document(
                "refresh-token-success",
                requestFields(refreshTokenRequestFields()),
                responseFields(tokenResultFields())
            ).returns(TokenResultDto::class.java)

        this.performGet(securedUrl(), accessToken = accessToken.token).expectOk()
    }


    @Test
    @Tag(TestTags.RESTDOC)
    fun `must not return new accessToken with expired refreshToken`() {

        mockUserAuthentication(refreshTokenTime = 1)
        Thread.sleep(2_000)

        this.performPost("$authUrl/refresh-token", body = RefreshAccessTokenRequest(refreshToken = refreshToken.token))
            .expectUnauthorized()

    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `should answer get who-am-i`() {

        mockUserAuthentication()

        val result: AccountDto = this.performGet("$authUrl/whoami", accessToken = accessToken.token)
            .expectOk()
            .document("who-am-i", responseFields(accountDtoResponseFields()))
            .returns()

        assertThat(account.id).isEqualTo(result.id)
        assertThat(account.email).isEqualTo(result.email)
        assertThat(account.email).isEqualTo(result.email)
    }


    @Test
    @Tag(TestTags.RESTDOC)
    fun `should access private endpoint with valid accessToken`() {
        mockUserAuthentication()
        this.performGet(securedUrl(), accessToken = accessToken.token).expectOk()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `must not access private endpoint without accessToken`() {
        mockUserAuthentication()
        this.performGet(securedUrl()).expectUnauthorized()
    }


    @Test
    @Tag(TestTags.RESTDOC)
    fun `must not access private endpoint with invalid accessToken`() {
        mockUserAuthentication(accessTokenTime = 1)
        Thread.sleep(1000)
        this.performGet(securedUrl(), accessToken = accessToken.token).expectUnauthorized()
    }

    private fun securedUrl(subjectId: UUID = account.persons.first().id): String =
        "/v1/messages/filter?receiverId=${subjectId}"


    private fun userSecretDtoResponseFields(prefix: String = ""): List<FieldDescriptor> {
        return arrayListOf(
            field(prefix + "id", STRING, "UUID"),
            field(prefix + "email", STRING, "An unique email"),
            field(prefix + "persons", ARRAY, "All persons of that Account"),
            field(prefix + "changeAccountToken", STRING, "").optional(),
            field(prefix + "changeAccountTokenCreatedAt", STRING, "").optional(),
            field(prefix + "persons", ARRAY, "persops"),

            ).apply {
            addAll(personFields(prefix + "persons[]."))
        }
    }

    private fun accountDtoResponseFields(): List<FieldDescriptor> {
        return arrayListOf(
            field("id", STRING, "UUID"),
            field("email", STRING, "An unique email"),
            field("persons", ARRAY, "All persons of that Account"),
        ).apply {
            addAll(personFields("persons[]."))
        }
    }

    private fun registerRequestFields(): List<FieldDescriptor> {
        return listOf(
            field("password", STRING, "A plain text password"),
            field("email", STRING, "A valid email"),
            field("name", STRING, "The fullname of the user"),
            field(
                "optionalGroupId", STRING,
                "When given: attaches to existing Group. If not: creating a new implicit Group"
            ).optional(),
        )
    }

    private fun loginRequestFields(): List<FieldDescriptor> {
        return listOf(
            field("password", STRING, "A plain text password"),
            field("email", STRING, "A valid email"),
        )
    }

    private fun loginResponseFields(prefix: String = ""): List<FieldDescriptor> {
        return listOf(
            field(prefix + "account", OBJECT, "The technical Account of this user"),
            field(prefix + "refreshToken", OBJECT, "Long living refreshToken for token generation"),
            field(prefix + "accessToken", OBJECT, "Short living accessToken for authentication and authorisation"),

            ).toMutableList().apply {
            addAll(userSecretDtoResponseFields("account."))
            addAll(tokenResultFields("refreshToken."))
            addAll(tokenResultFields("accessToken."))
        }
    }

    private fun refreshTokenRequestFields(): List<FieldDescriptor> {
        return listOf(
            field(
                "refreshToken",
                STRING,
                "Necessary refreshToken (obtained during login) for getting a new accessToken"
            ),
        )
    }

    private fun tokenResultFields(prefix: String = ""): List<FieldDescriptor> {
        return listOf(
            field(prefix + "token", STRING, "Effective signed JWT token for authentication "),
            field(prefix + "subject", STRING, "Account's email for faster validations"),
            field(prefix + "issuedAt", STRING, "DateTime of issuing"),
            field(prefix + "expiration", STRING, "DateTime of expiration: token must be refreshed before"),
            field(prefix + "issuer", STRING, "Authority which granted this JWT"),
        )
    }

    private fun updateProfileRequestFields(): List<FieldDescriptor> {
        return listOf(
            field("name", STRING, "The fullname of the user"),
        )
    }

}
