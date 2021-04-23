package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.ossiaustria.amigo.platform.ApplicationProfiles
import org.ossiaustria.amigo.platform.config.security.JwtService
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.repositories.AccountRepository
import org.ossiaustria.amigo.platform.repositories.PersonRepository
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.ossiaustria.amigo.platform.services.auth.TokenResult
import org.ossiaustria.amigo.platform.testcommons.AbstractRestTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.jdbc.core.JdbcTemplate
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import javax.persistence.EntityManager
import javax.transaction.Transactional

@TestPropertySource("classpath:application.yml")
@ExtendWith(value = [RestDocumentationExtension::class, SpringExtension::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(ApplicationProfiles.TEST)
@ComponentScan("org.ossiaustria.amigo.platform")
@AutoConfigureTestDatabase(connection = org.springframework.boot.jdbc.EmbeddedDatabaseConnection.H2)
//@ContextConfiguration(initializers = [TestPostgresContainer.Initializer::class])
internal abstract class AbstractRestApiTest : AbstractRestTest() {


    @MockkBean(relaxed = true, relaxUnitFun = true)
    protected lateinit var currentUserService: CurrentUserService

    @Autowired
    protected lateinit var personRepository: PersonRepository

    @Autowired
    protected lateinit var jwtService: JwtService

    @Autowired
    protected lateinit var accountRepository: AccountRepository

    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()

    @Autowired
    protected lateinit var accountSubjectPreparationTrait: AccountSubjectPreparationTrait


    @Autowired
    val jdbcTemplate: JdbcTemplate? = null

    @Autowired
    val entityManager: EntityManager? = null

    protected lateinit var accessToken: TokenResult
    protected lateinit var account: Account
    protected lateinit var refreshToken: TokenResult

    protected fun defaultAcceptContentAuth(
        builder: MockHttpServletRequestBuilder,
        token: String
    ): MockHttpServletRequestBuilder {
        return this.acceptContentAuth(builder, token)
    }

    protected fun truncateDbTables(tables: List<String>, cascade: Boolean = true) {
        println("Truncating tables: $tables")
        val joinToString = tables.joinToString("\", \"", "\"", "\"")

        try {
            if (cascade) {
                entityManager!!.createNativeQuery("truncate table $joinToString CASCADE ").executeUpdate()
            } else {
                entityManager!!.createNativeQuery("truncate table $joinToString ").executeUpdate()
            }
        } catch (e: Exception) {
        }
    }

    protected fun truncateAllTables() {
        truncateDbTables(
            listOf(
                "account",
                "person",
                "message",
            ), cascade = true
        )
    }

    @BeforeEach
    fun setUp(
        webApplicationContext: WebApplicationContext,
        restDocumentation: RestDocumentationContextProvider
    ) {
        truncateAllTables()
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
//        every { currentUserService.person() } answers { personRepository.findAll().first() }
        every { currentUserService.account() } answers { account }
    }

//    fun mockTokenUserDetails(
//        accountId: UUID,
//        email: String,
//        issuedAt: Long = -1000,
//        expiration: Long = 1000,
//    ) = TokenUserDetails(
//        accountId = accountId,
//        email = email,
//        personsIds = listOf(),
//        issuedAt = Date(System.currentTimeMillis() + issuedAt),
//        expiration = Date(System.currentTimeMillis() + expiration)
//    )

//    fun mockSecurityContextHolder(tokenUser: TokenUserDetails) {
//        mockUserAuthentication()
//        val secContext = mockk<SecurityContext>()
//        val authentication = mockk<Authentication>()
//
//        every { authentication.principal } answers { tokenUser }
//        every { secContext.authentication } answers {
//            UsernamePasswordAuthenticationToken(
//                accessToken.token,
//                accessToken.token
//            )
//        }
//
//        SecurityContextHolder.setContext(secContext)
//    }

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
    fun createMockUser(plainPassword: String = "password", userOverrideSuffix: String? = null): Account {
        return accountSubjectPreparationTrait.createMockAccount(plainPassword, userOverrideSuffix)
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

    fun commitAndFail(f: () -> Unit) {
        assertThrows<Exception> {
            withinTransaction {
                f.invoke()
            }
        }
    }

    fun <T> withinTransaction(commit: Boolean = true, func: () -> T): T {
        if (!TestTransaction.isActive()) TestTransaction.start()
        val result = func.invoke()
        if (commit) {
            TestTransaction.flagForCommit()
        } else {
            TestTransaction.flagForRollback()
        }
        try {
            TestTransaction.end()
        } catch (e: Exception) {
            throw e
        }
        return result
    }
}

fun FieldDescriptor.copy(path: String? = null): FieldDescriptor {
    return fieldWithPath(path ?: this.path)
        .type(this.type)
        .description(this.description)
        .also {
            if (this.isOptional) it.optional()
        }
}
