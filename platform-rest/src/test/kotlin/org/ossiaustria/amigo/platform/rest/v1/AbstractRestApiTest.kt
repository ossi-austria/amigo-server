package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.ossiaustria.amigo.platform.domain.config.ApplicationProfiles
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.ossiaustria.amigo.platform.domain.services.auth.JwtService
import org.ossiaustria.amigo.platform.domain.services.auth.TokenResult
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.ossiaustria.amigo.platform.testcommons.AbstractRestTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.operation.preprocess.Preprocessors.removeHeaders
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.snippet.Snippet
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.UUID
import javax.transaction.Transactional

@TestPropertySource("classpath:application-test.yml")
@ExtendWith(value = [RestDocumentationExtension::class, SpringExtension::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(ApplicationProfiles.TEST)
@ComponentScan("org.ossiaustria.amigo.platform")
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
internal abstract class AbstractRestApiTest : AbstractRestTest() {

    @MockkBean(relaxed = true, relaxUnitFun = true)
    protected lateinit var currentUserService: CurrentUserService

    @Autowired
    protected lateinit var jwtService: JwtService

    @Autowired
    protected lateinit var authService: AuthService

    @Autowired
    protected lateinit var accountSubjectPreparationTrait: AccountSubjectPreparationTrait

    protected lateinit var accessToken: TokenResult
    protected lateinit var account: Account
    protected lateinit var account2: Account
    protected lateinit var group: Group
    protected lateinit var refreshToken: TokenResult
    protected lateinit var person1Id: UUID
    protected lateinit var person2Id: UUID
    protected lateinit var person1: Person
    protected lateinit var person2: Person

    @BeforeEach
    fun setUp(
        webApplicationContext: WebApplicationContext,
        restDocumentation: RestDocumentationContextProvider
    ) {

        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .apply<DefaultMockMvcBuilder>(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(
                        removeHeaders(HEADER_PRIVATE_TOKEN),
                        Preprocessors.prettyPrint(),
                    )
                    .withResponseDefaults(Preprocessors.prettyPrint())
            ).build()

        accountSubjectPreparationTrait.apply()
        account = accountSubjectPreparationTrait.account
        account2 = accountSubjectPreparationTrait.account2
        group = accountSubjectPreparationTrait.group!!
        every { currentUserService.account() } answers { account }

        person1 = account.primaryPerson()
        person1Id = person1.id
        person2 = accountSubjectPreparationTrait.person2
        person2Id = person2.id
    }


    fun forbiddenGET(url: String) {
        mockUserAuthentication()
        this.performGet(url).expectUnauthorized()

        mockUserAuthentication(accessTokenTime = 1)
        Thread.sleep(1000)
        this.performGet(url, accessToken = accessToken.token).expectUnauthorized()
    }


    fun mockUserAuthentication(refreshTokenTime: Long = 500, accessTokenTime: Long = 100): TokenResult {
        refreshToken = jwtService.generateRefreshToken(account.id, account.email, refreshTokenTime)
        accessToken =
            jwtService.generateAccessToken(
                account.id,
                account.email,
                accessTokenTime,
                personsIds = account.persons.map { it.id })
        return accessToken
    }

    @Transactional
    fun createMockUser(plainPassword: String = "password", userOverrideSuffix: String = ""): Account {
        return accountSubjectPreparationTrait.createMockAccount(plainPassword, userOverrideSuffix)
    }

    @Transactional
    fun createMockGroup(): Group {
        return accountSubjectPreparationTrait.createMockGroup(account)
    }

    fun ResultActions.document(name: String, vararg snippets: Snippet): ResultActions {
        return this.andDo(MockMvcRestDocumentation.document(name, *snippets))
    }

    protected fun errorResponseFields(): List<FieldDescriptor> {
        return listOf(
            fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("Unique error code"),
            fieldWithPath("errorName").type(JsonFieldType.STRING).description("Short error title"),
            fieldWithPath("errorMessage").type(JsonFieldType.STRING).description("A detailed message"),
            fieldWithPath("time").type(JsonFieldType.STRING).description("Timestamp of error")
        )
    }

}
