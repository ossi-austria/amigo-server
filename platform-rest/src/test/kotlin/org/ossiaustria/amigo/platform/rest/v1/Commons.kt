package org.ossiaustria.amigo.platform.rest.v1

import org.ossiaustria.amigo.platform.repositories.AccountRepository
import org.ossiaustria.amigo.platform.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.utils.RandomUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID
import javax.transaction.Transactional

object TestTags {
    const val SLOW = "slow"
    const val UNIT = "unit"
    const val INTEGRATION = "integration"
    const val RESTDOC = "restdoc"
}

internal fun projectResponseFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "global_slug").optional().type(JsonFieldType.STRING)
            .description("Global Slug must be unique for the whole platform"),
        fieldWithPath(prefix + "visibility_scope").type(JsonFieldType.STRING).description("Visibility scope"),
        fieldWithPath(prefix + "name").type(JsonFieldType.STRING)
            .description("A Name which is unique per scope (owner's domain)"),
        fieldWithPath(prefix + "description").type(JsonFieldType.STRING).description("Text for description"),
        fieldWithPath(prefix + "tags").type(JsonFieldType.ARRAY).description("All Tags for this Project"),
        fieldWithPath(prefix + "owner_id").type(JsonFieldType.STRING)
            .description("UUID of Subject who owns this Project"),
        fieldWithPath(prefix + "stars_count").type(JsonFieldType.NUMBER).description("Number of Stars"),
        fieldWithPath(prefix + "forks_count").type(JsonFieldType.NUMBER).description("Number of Forks"),
        fieldWithPath(prefix + "input_data_types").type(JsonFieldType.ARRAY)
            .description("List of DataTypes used for Input"),
        fieldWithPath(prefix + "output_data_types").type(JsonFieldType.ARRAY)
            .description("List of DataTypes used for Output"),
        fieldWithPath(prefix + "searchable_type").type(JsonFieldType.STRING).description("Type of searchable Entity"),
        fieldWithPath(prefix + "id").type(JsonFieldType.STRING).description("Data project id"),
        fieldWithPath(prefix + "slug").type(JsonFieldType.STRING).description("Data project slug"),
        fieldWithPath(prefix + "url").type(JsonFieldType.STRING).description("URL in Gitlab domain"),
        fieldWithPath(prefix + "owner_id").type(JsonFieldType.STRING).description("Onwer id of the data project"),
        fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("Project name"),
        fieldWithPath(prefix + "gitlab_namespace").type(JsonFieldType.STRING)
            .description("The group/namespace where the project is in"),
        fieldWithPath(prefix + "gitlab_path").type(JsonFieldType.STRING).description("Project path"),
        fieldWithPath(prefix + "gitlab_id").type(JsonFieldType.NUMBER).description("Id in gitlab"),
        fieldWithPath(prefix + "published").optional().type(JsonFieldType.BOOLEAN).description("Project is published"),
    ).apply {
        this.add(
            fieldWithPath(prefix + "data_processor").optional().type(JsonFieldType.OBJECT).description("DataProcessor")
        )
        this.addAll(dataProcessorFields(prefix + "data_processor."))
        this.addAll(searchableTags(prefix + "tags[]."))
    }.apply {
        this.add(fieldWithPath(prefix + "experiments").optional().type(JsonFieldType.ARRAY).description("Experiments"))
    }
}

internal fun searchableTags(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "id").type(JsonFieldType.STRING).optional().description("Unique UUID"),
        fieldWithPath(prefix + "name").optional().type(JsonFieldType.STRING).optional()
            .description("Name of Tag, unique, useful and readable"),
        fieldWithPath(prefix + "type").type(JsonFieldType.STRING).optional().description("Type or Family of this Tag"),
        fieldWithPath(prefix + "public").type(JsonFieldType.BOOLEAN).optional()
            .description("Flag indicating whether this is public or not")
    )
}

internal fun searchableTagsRequestFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "id").type(JsonFieldType.STRING).optional().description("Unique UUID"),
        fieldWithPath(prefix + "owner_id").optional().type(JsonFieldType.STRING).optional()
            .description("Nullable owner_id"),
        fieldWithPath(prefix + "name").optional().type(JsonFieldType.STRING).optional()
            .description("Name of Tag, unique, useful and readable"),
        fieldWithPath(prefix + "type").type(JsonFieldType.STRING).optional().description("Type or Family of this Tag"),
        fieldWithPath(prefix + "public").type(JsonFieldType.BOOLEAN).optional()
            .description("Flag indicating whether this is public or not")
    )
}

internal fun projectUpdateRequestFields(): List<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath("description").type(JsonFieldType.STRING).optional().description("Description of Project"),
        fieldWithPath("name").type(JsonFieldType.STRING).optional().description("Name of Project"),
        fieldWithPath("visibility").type(JsonFieldType.STRING).optional().description("Visibility of Project"),
        fieldWithPath("input_data_types").type(JsonFieldType.ARRAY).optional()
            .description("List of DataTypes for input"),
        fieldWithPath("output_data_types").type(JsonFieldType.ARRAY).optional()
            .description("List of DataTypes for output"),
        fieldWithPath("tags").type(JsonFieldType.ARRAY).optional().description("List of Tags")
    ).apply {
        addAll(searchableTagsRequestFields("tags[]."))
    }
}

internal fun dataProcessorInstanceFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "id").type(JsonFieldType.STRING).optional()
            .description("Unique UUID of this DataProcessor"),
        fieldWithPath(prefix + "slug").type(JsonFieldType.STRING).optional()
            .description("Unique slug of this DataProcessor"),
        fieldWithPath(prefix + "name").optional().type(JsonFieldType.STRING).optional()
            .description("Optional Name of this DataProcessor ( not needed in Inputs)"),
        fieldWithPath(prefix + "parameters").type(JsonFieldType.ARRAY).optional().description("Name of Parameter"),
        fieldWithPath(prefix + "parameters[].name").type(JsonFieldType.STRING).optional()
            .description("Name of Parameter"),
        fieldWithPath(prefix + "parameters[].type").type(JsonFieldType.STRING).optional()
            .description("Provided ParameterType of this Parameter"),
        fieldWithPath(prefix + "parameters[].required").type(JsonFieldType.BOOLEAN).optional()
            .description("Parameter required?"),
        fieldWithPath(prefix + "parameters[].description").type(JsonFieldType.STRING).optional()
            .description("Textual description of this Parameter"),
        fieldWithPath(prefix + "parameters[].value").type(JsonFieldType.STRING).optional()
            .description("Provided value (as parsable String) of Parameter ")
    )
}

fun pageableResourceParameters(): Array<ParameterDescriptor> {
    return arrayOf(
        RequestDocumentation.parameterWithName("page").optional().description("Page number (starting from 0)"),
        RequestDocumentation.parameterWithName("size").optional().description("Number elements on the page"),
        RequestDocumentation.parameterWithName("sort").optional().description("Sort by field (eg. &sort=id,asc)")
    )
}

fun wrapToPage(content: List<FieldDescriptor>): List<FieldDescriptor> {
    return mutableListOf(
        fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("Is the last page"),
        fieldWithPath("total_pages").type(JsonFieldType.NUMBER).description("Total pages count"),
        fieldWithPath("total_elements").type(JsonFieldType.NUMBER)
            .description("Total elements count ([pages count] x [page size])"),
        fieldWithPath("size").type(JsonFieldType.NUMBER)
            .description("Requested elements count per page. Request parameter 'size'. Default 20"),
        fieldWithPath("number").type(JsonFieldType.NUMBER).description("Current page number"),
        fieldWithPath("number_of_elements").type(JsonFieldType.NUMBER).description("Elements count in current page"),
        fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("Is the first page"),
        fieldWithPath("empty").type(JsonFieldType.BOOLEAN).description("Is the current page empty")
    ).apply {
        addAll(content.map { it.copy("content[].${it.path}") })
        addAll(pageableFields())
        addAll(sortFields())
    }
}

private fun pageableFields(): List<FieldDescriptor> {
    val prefix = "pageable."
    return mutableListOf(
        fieldWithPath(prefix + "offset").type(JsonFieldType.NUMBER)
            .description("Current offset (starting from 0). Request parameter 'page' or 'offset'"),
        fieldWithPath(prefix + "page_size").type(JsonFieldType.NUMBER)
            .description("Requested elements count per page. Request parameter 'size'. Default 20"),
        fieldWithPath(prefix + "page_number").type(JsonFieldType.NUMBER).description("Current page number"),
        fieldWithPath(prefix + "unpaged").type(JsonFieldType.BOOLEAN).description("Is the result unpaged"),
        fieldWithPath(prefix + "paged").type(JsonFieldType.BOOLEAN).description("Is the result paged")
    ).apply {
        addAll(sortFields(prefix))
    }
}

private fun sortFields(prefix: String = ""): List<FieldDescriptor> {
    return listOf(
        fieldWithPath(prefix + "sort.sorted").type(JsonFieldType.BOOLEAN)
            .description("Is the result sorted. Request parameter 'sort', values '=field,direction(asc,desc)'"),
        fieldWithPath(prefix + "sort.unsorted").type(JsonFieldType.BOOLEAN).description("Is the result unsorted"),
        fieldWithPath(prefix + "sort.empty").type(JsonFieldType.BOOLEAN).description("Is the sort empty")
    )
}

internal fun pageable(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "content").type(JsonFieldType.ARRAY).optional().description(""),
        fieldWithPath(prefix + "pageable.sort").type(JsonFieldType.OBJECT).optional().description(""),
        fieldWithPath(prefix + "pageable.sort.unsorted").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "pageable.sort.sorted").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "pageable.sort.empty").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "pageable.page_size").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "pageable.page_number").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "pageable.offset").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "pageable.paged").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "pageable.unpaged").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "total_elements").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "total_pages").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "last").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "first").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "number_of_elements").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "sort").type(JsonFieldType.OBJECT).optional().description(""),
        fieldWithPath(prefix + "sort.unsorted").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "sort.sorted").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "sort.empty").type(JsonFieldType.BOOLEAN).optional().description(""),
        fieldWithPath(prefix + "size").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "number").type(JsonFieldType.NUMBER).optional().description(""),
        fieldWithPath(prefix + "empty").type(JsonFieldType.BOOLEAN).optional().description("")
    )
}

internal fun dataProcessorFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "id").type(JsonFieldType.STRING).description("Unique UUID of this DataProcessor"),
        fieldWithPath(prefix + "slug").type(JsonFieldType.STRING).description("Unique slug of this DataProcessor"),
        fieldWithPath(prefix + "name").optional().type(JsonFieldType.STRING)
            .description("Optional Name of this DataProcessor ( not needed in Inputs)"),
        fieldWithPath(prefix + "input_data_type").type(JsonFieldType.STRING).description("DataType for input data"),
        fieldWithPath(prefix + "output_data_type").type(JsonFieldType.STRING).description("DataType for output data"),
        fieldWithPath(prefix + "type").type(JsonFieldType.STRING).description("ALGORITHM, OPERATION or VISUALIZATION"),
        fieldWithPath(prefix + "visibility_scope").type(JsonFieldType.STRING).optional()
            .description("PUBLIC or PRIVATE"),
        fieldWithPath(prefix + "description").optional().type(JsonFieldType.STRING).description("Description"),
        fieldWithPath(prefix + "code_project_id").type(JsonFieldType.STRING).optional()
            .description("CodeProject this Processor belongs to"),
        fieldWithPath(prefix + "author_id").optional().type(JsonFieldType.STRING).optional()
            .description("Author who created this")
    ).apply {
        add(
            fieldWithPath(prefix + "versions").optional().type(JsonFieldType.ARRAY)
                .description("Data processor versions")
        )
        addAll(processorVersionFields(prefix + "versions[]."))
    }
}

internal fun processorVersionFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "id").type(JsonFieldType.STRING).description("Unique UUID of this DataProcessorVersion"),
        fieldWithPath(prefix + "data_processor_id").type(JsonFieldType.STRING)
            .description("Unique UUID of this DataProcessor"),
        fieldWithPath(prefix + "slug").type(JsonFieldType.STRING).description("Unique slug of this DataProcessor"),
        fieldWithPath(prefix + "name").optional().type(JsonFieldType.STRING)
            .description("Optional Name of this DataProcessor ( not needed in Inputs)"),
        fieldWithPath(prefix + "number").optional().type(JsonFieldType.NUMBER)
            .description("Relative number of this DataProcessor Version"),
        fieldWithPath(prefix + "branch").optional().type(JsonFieldType.STRING)
            .description("Branch this Version was built on"),
        fieldWithPath(prefix + "command").optional().type(JsonFieldType.STRING)
            .description("Python command to execute"),
//        fieldWithPath(prefix + "base_environment").optional().type(JsonFieldType.STRING).description("Identifier of BaseEnvironment"),
        fieldWithPath(prefix + "published_at").optional().type(JsonFieldType.STRING)
            .description("Timestamp of publication"),
        fieldWithPath(prefix + "input_data_type").type(JsonFieldType.STRING).description("DataType for input data"),
        fieldWithPath(prefix + "output_data_type").type(JsonFieldType.STRING).description("DataType for output data"),
        fieldWithPath(prefix + "type").type(JsonFieldType.STRING).description("ALGORITHM, OPERATION or VISUALIZATION"),
        fieldWithPath(prefix + "visibility_scope").type(JsonFieldType.STRING).optional()
            .description("PUBLIC or PRIVATE"),
        fieldWithPath(prefix + "description").optional().type(JsonFieldType.STRING).description("Description"),
        fieldWithPath(prefix + "code_project_id").type(JsonFieldType.STRING).optional()
            .description("CodeProject this Processor belongs to"),
        fieldWithPath(prefix + "author_id").optional().type(JsonFieldType.STRING).optional()
            .description("Author who created this"),
        fieldWithPath(prefix + "publisher_id").optional().type(JsonFieldType.STRING).optional()
            .description("Author who created this"),
        fieldWithPath(prefix + "metric_type").type(JsonFieldType.STRING).description("Type of Metric"),
        fieldWithPath(prefix + "parameters").type(JsonFieldType.ARRAY).optional().description("Name of Parameter"),
        fieldWithPath(prefix + "parameters[].name").type(JsonFieldType.STRING).optional()
            .description("Name of Parameter"),
        fieldWithPath(prefix + "parameters[].type").type(JsonFieldType.STRING).optional()
            .description("Provided ParameterType of this Parameter"),
        fieldWithPath(prefix + "parameters[].order").type(JsonFieldType.NUMBER).optional()
            .description("Provided ParameterType of this Parameter"),
        fieldWithPath(prefix + "parameters[].default_value").type(JsonFieldType.STRING).optional()
            .description("Provided value (as parsable String) of Parameter "),
        fieldWithPath(prefix + "parameters[].required").type(JsonFieldType.BOOLEAN).optional()
            .description("Parameter required?"),
        fieldWithPath(prefix + "parameters[].description").type(JsonFieldType.STRING).optional()
            .description("Textual description of this Parameter"),
        fieldWithPath(prefix + "pipeline_job_info").optional().type(JsonFieldType.OBJECT).optional()
            .description("Gitlab Pipeline information")
    ).apply {
        addAll(pipelineInfoDtoResponseFields(prefix + "pipeline_job_info."))
    }.apply {
        addAll(environmentsFields(prefix + "base_environment."))
    }
}

internal fun pipelineInfoDtoResponseFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).optional()
            .description("Json object describing specific metrics"),
        fieldWithPath(prefix + "commit_sha").type(JsonFieldType.STRING).optional()
            .description("Json object describing specific metrics"),
        fieldWithPath(prefix + "ref").type(JsonFieldType.STRING).optional()
            .description("Json object describing specific metrics"),
        fieldWithPath(prefix + "committed_at").type(JsonFieldType.STRING).optional()
            .description("Timestamp when the gitlab pipeline was committed"),
        fieldWithPath(prefix + "created_at").type(JsonFieldType.STRING).optional()
            .description("Timestamp when the gitlab pipeline was created"),
        fieldWithPath(prefix + "started_at").type(JsonFieldType.STRING).optional()
            .description("Timestamp when the gitlab pipeline was started"),
        fieldWithPath(prefix + "updated_at").type(JsonFieldType.STRING).optional()
            .description("Timestamp when the gitlab pipeline was updated"),
        fieldWithPath(prefix + "finished_at").type(JsonFieldType.STRING).optional()
            .description("Timestamp when the gitlab pipeline was finished")
    )
}

internal fun fileLocationsFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "location").type(JsonFieldType.STRING)
            .description("A URL, URI or simple path describing the location of a file/folder"),
        fieldWithPath(prefix + "location_type").type(JsonFieldType.STRING).description("PATH, URL or AWS_ID ")
    )
}

internal fun commitFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return mutableListOf(
        fieldWithPath(prefix + "author_email").optional().type(JsonFieldType.STRING).description("User's email"),
        fieldWithPath(prefix + "author_name").optional().type(JsonFieldType.STRING).description("Username"),
        fieldWithPath(prefix + "authored_date").optional().type(JsonFieldType.STRING).description("Date of publish"),
        fieldWithPath(prefix + "committer_email").optional().type(JsonFieldType.STRING).description("Commiter email"),
        fieldWithPath(prefix + "committer_name").optional().type(JsonFieldType.STRING).description("Commiter name"),
        fieldWithPath(prefix + "committed_date").optional().type(JsonFieldType.STRING).description("Date of commit"),
        fieldWithPath(prefix + "title").type(JsonFieldType.STRING).description("Commit title"),
        fieldWithPath(prefix + "message").type(JsonFieldType.STRING).description("Commit message"),
        fieldWithPath(prefix + "id").optional().type(JsonFieldType.STRING).description("Username"),
        fieldWithPath(prefix + "short_id").optional().type(JsonFieldType.STRING).description("Username"),
    )
}

internal fun publishingProcessFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return mutableListOf(
        fieldWithPath(prefix + "id").optional().type(JsonFieldType.STRING).description("Data processor id"),
        fieldWithPath(prefix + "branch").optional().type(JsonFieldType.STRING).description("Published branch"),
        fieldWithPath(prefix + "command").optional().type(JsonFieldType.STRING).description("Command"),
        fieldWithPath(prefix + "path").optional().type(JsonFieldType.STRING).description("Main script path"),
        fieldWithPath(prefix + "publish_info").optional().type(JsonFieldType.OBJECT)
            .description("Information about commit"),
    ).apply {
        addAll(publishingInfoFields(prefix + "publish_info."))
    }.apply {
        addAll(environmentsFields(prefix + "environment."))
    }
}

internal fun publishingInfoFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return arrayListOf(
        fieldWithPath(prefix + "commit_sha").type(JsonFieldType.STRING).optional().description("Publishing commit sha"),
        fieldWithPath(prefix + "published_at").type(JsonFieldType.STRING).optional()
            .description("Timestamp when the project was published"),
        fieldWithPath(prefix + "published_by").type(JsonFieldType.STRING).optional().description("Published person id"),
    )
}

internal fun environmentsFields(prefix: String = ""): MutableList<FieldDescriptor> {
    return mutableListOf(
        fieldWithPath(prefix + "id").optional().type(JsonFieldType.STRING).description("Environment id"),
        fieldWithPath(prefix + "title").optional().type(JsonFieldType.STRING).description("Title"),
        fieldWithPath(prefix + "description").type(JsonFieldType.STRING).optional().description("Description"),
        fieldWithPath(prefix + "requirements").type(JsonFieldType.STRING).optional()
            .description("Library requirements"),
        fieldWithPath(prefix + "docker_image").optional().type(JsonFieldType.STRING).description("Docker image"),
        fieldWithPath(prefix + "machine_type").optional().type(JsonFieldType.STRING).description("Machine type"),
        fieldWithPath(prefix + "sdk_version").optional().type(JsonFieldType.STRING)
            .description("SDK version (Python, Java etc)"),
    )
}


@Component
internal class AccountSubjectPreparationTrait {

    lateinit var account: Account
    lateinit var account2: Account
    lateinit var subject: Person
    lateinit var subject2: Person
    var token: String = "test-dummy-token-"
    lateinit var token2: String


    @Autowired
    protected lateinit var personRepository: PersonRepository

    @Autowired
    protected lateinit var accountRepository: AccountRepository

    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()

    fun apply() {
        deleteAll()
        applyAccount()
    }

    fun applyAccount() {
        account = createMockUser(personGitlabId = 1L)
        account2 = createMockUser(userOverrideSuffix = "0002", personGitlabId = 2L)
        subject = account.persons.first()
        subject2 = account2.persons.first()
        token = RandomUtils.generateRandomUserName(25)
        token2 = RandomUtils.generateRandomUserName(25)
    }

    protected fun deleteAll() {
        accountRepository.deleteAll()
        personRepository.deleteAll()
    }

    @Transactional
    protected fun createMockUser(
        plainPassword: String = "password",
        userOverrideSuffix: String? = null,
        personGitlabId: Long? = null
    ): Account {

        var mockToken = AbstractRestApiTest.testPrivateUserTokenMock1
        var userSuffix = "0000"
        if (userOverrideSuffix != null) {
            userSuffix = userOverrideSuffix
            mockToken = "second-token-$userSuffix"
        }
        val passwordEncrypted = passwordEncoder.encode(plainPassword)

        val person = personRepository.save(
            Person(
                id = randomUUID(),
                name = "user name $userSuffix",
                groupId = randomUUID()
            )
        )
        val account = accountRepository.save(
            Account(
                id = randomUUID(),
                email = "email$userSuffix@example.com",
                passwordEncrypted = passwordEncrypted,
                jwtToken = mockToken,
                persons = listOf(person)
            )
        )
        return account
    }
}

