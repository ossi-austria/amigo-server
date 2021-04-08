package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.ossiaustria.amigo.platform.ApplicationProfiles
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.repositories.AccountRepository
import org.ossiaustria.amigo.platform.repositories.PersonRepository
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.ossiaustria.amigo.platform.security.APSessionRegistry
import org.ossiaustria.amigo.platform.services.auth.TokenDetails
import org.ossiaustria.amigo.platform.testcommons.AbstractRestTest
import org.ossiaustria.amigo.platform.testcommons.TestPostgresContainer
import org.ossiaustria.amigo.platform.testcommons.TestRedisContainer
import org.ossiaustria.amigo.platform.utils.RandomUtils.generateRandomUserName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.*
import java.util.regex.Pattern
import javax.persistence.EntityManager
import javax.transaction.Transactional

@TestPropertySource("classpath:application.yml")
@ExtendWith(value = [RestDocumentationExtension::class, SpringExtension::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(ApplicationProfiles.TEST)
@ComponentScan("org.ossiaustria.amigo.platform")
//@AutoConfigureTestDatabase(connection = org.springframework.boot.jdbc.EmbeddedDatabaseConnection.H2)
@ContextConfiguration(initializers = [TestPostgresContainer.Initializer::class, TestRedisContainer.Initializer::class])
abstract class AbstractRestApiTest : AbstractRestTest() {

    protected lateinit var account: Account
    protected var token: String = "test-dummy-token-"

    companion object {
        const val testPrivateUserTokenMock1: String = "doesnotmatterat-all-11111"
    }


    @MockkBean(relaxed = true, relaxUnitFun = true)
    protected lateinit var currentUserService: CurrentUserService

    @MockkBean(relaxed = true, relaxUnitFun = true)
    protected lateinit var sessionRegistry: APSessionRegistry

    @Autowired
    protected lateinit var personRepository: PersonRepository

    @Autowired
    protected lateinit var accountRepository: AccountRepository

    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()

    protected fun defaultAcceptContentAuth(
        builder: MockHttpServletRequestBuilder,
        token: String
    ): MockHttpServletRequestBuilder {
        return this.acceptContentAuth(builder, token)
    }

    @Autowired
    val jdbcTemplate: JdbcTemplate? = null

    @Autowired
    val entityManager: EntityManager? = null

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
                "subject",
            ), cascade = true
        )
    }

    @BeforeEach
    fun setUp(
        webApplicationContext: WebApplicationContext,
        restDocumentation: RestDocumentationContextProvider
    ) {
        truncateAllTables()
        val censoredSecretHash = testPrivateUserTokenMock1.substring(0, 5) + "**********"
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .apply<DefaultMockMvcBuilder>(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(
                        removeHeaders(HEADER_PRIVATE_TOKEN),
                        Preprocessors.prettyPrint(),
                        Preprocessors.replacePattern(Pattern.compile(testPrivateUserTokenMock1), censoredSecretHash)
                    )
                    .withResponseDefaults(
                        Preprocessors.prettyPrint(),
                        Preprocessors.replacePattern(Pattern.compile(testPrivateUserTokenMock1), censoredSecretHash)
                    )
            )
            .build()


        every { currentUserService.person() } answers { personRepository.findAll().first() }
        every { currentUserService.account() } answers { accountRepository.findAll().first() }
        every { currentUserService.accessToken() } answers { testPrivateUserTokenMock1 }

    }


    fun mockSecurityContextHolder(token: TokenDetails? = null) {
        val finalToken = token ?: TokenDetails(
            "testusername",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "testusername",
        )

        val secContext = mockk<SecurityContext>()
        val authentication = mockk<Authentication>()

        every { authentication.principal } answers { finalToken }
        every { secContext.authentication } answers { UsernamePasswordAuthenticationToken(token, token) }
        every { sessionRegistry.retrieveFromSession(any()) } answers { finalToken }

        SecurityContextHolder.setContext(secContext)
    }

    fun mockUserAuthentication(
        returnAccount: Account? = null
    ) {
        val actualAccount = returnAccount ?: account
        every { sessionRegistry.retrieveFromSession(any()) } answers {
            val token = this.args[0] as String
            tokenDetails(actualAccount, token)
        }
    }

    @Transactional
    fun createMockUser(plainPassword: String = "password", userOverrideSuffix: String? = null): Account {
        val accountId = UUID.randomUUID()
        val passwordEncrypted = passwordEncoder.encode(plainPassword)

        return accountRepository.save(
            Account(
                accountId,
                "${generateRandomUserName(30)}@example.com",
                passwordEncrypted
            )
        )
    }

    private fun tokenDetails(
        actualAccount: Account,
        token: String
    ): TokenDetails {
        return TokenDetails(
            username = actualAccount.email,
            accessToken = token,
            accountId = actualAccount.id,
            personId = actualAccount.id,
        )
    }

    fun ResultActions.document(name: String, vararg snippets: Snippet): ResultActions {
        return this.andDo(MockMvcRestDocumentation.document(name, *snippets))
    }

    protected fun errorResponseFields(): List<FieldDescriptor> {
        return listOf(
            fieldWithPath("error_code").type(JsonFieldType.NUMBER).description("Unique error code"),
            fieldWithPath("error_name").type(JsonFieldType.STRING).description("Short error title"),
            fieldWithPath("error_message").type(JsonFieldType.STRING).description("A detailed message"),
            fieldWithPath("time").type(JsonFieldType.STRING).description("Timestamp of error")
        )
    }

    private fun sortFields(prefix: String = ""): List<FieldDescriptor> {
        return listOf(
            fieldWithPath(prefix + "sort.sorted").type(JsonFieldType.BOOLEAN)
                .description("Is the result sorted. Request parameter 'sort', values '=field,direction(asc,desc)'"),
            fieldWithPath(prefix + "sort.unsorted").type(JsonFieldType.BOOLEAN).description("Is the result unsorted"),
            fieldWithPath(prefix + "sort.empty").type(JsonFieldType.BOOLEAN).description("Is the sort empty")
        )
    }

    protected fun experimentDtoResponseFields(prefix: String = ""): List<FieldDescriptor> {
        return listOf(
            fieldWithPath(prefix + "id").type(JsonFieldType.STRING).description("UUID"),
            fieldWithPath(prefix + "data_project_id").type(JsonFieldType.STRING).description("Id of DataProject"),
            fieldWithPath(prefix + "data_instance_id").optional().type(JsonFieldType.STRING)
                .description("Id of DataPipelineInstance"),
            fieldWithPath(prefix + "slug").type(JsonFieldType.STRING).description("Local slug scoped to DataProject"),
            fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("Name of that Experiment"),
            fieldWithPath(prefix + "number").type(JsonFieldType.NUMBER)
                .description("Number of this Experiment in its DataProject scope"),
            fieldWithPath(prefix + "pipeline_job_info").type(JsonFieldType.OBJECT).optional()
                .description("An optional PipelineInfo describing the gitlab pipeline info"),
            fieldWithPath(prefix + "json_blob").type(JsonFieldType.STRING).optional()
                .description("Json object describing experiments epochs statistics"),
            fieldWithPath(prefix + "post_processing").optional().type(JsonFieldType.ARRAY).optional()
                .description("An optional List of DataProcessors used during PostProcessing"),
            fieldWithPath(prefix + "processing").optional().type(JsonFieldType.OBJECT).optional()
                .description("An optional DataAlgorithm"),
            fieldWithPath(prefix + "status").type(JsonFieldType.STRING).description("Status of experiment"),
            fieldWithPath(prefix + "source_branch").type(JsonFieldType.STRING).description("Branch name"),
            fieldWithPath(prefix + "target_branch").type(JsonFieldType.STRING).description("Branch name")
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
