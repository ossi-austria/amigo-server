package org.ossiaustria.amigo.platform.rest.v1

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.services.GroupService
import org.ossiaustria.amigo.platform.rest.v1.groups.GroupDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType.ARRAY
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.requestParameters

internal class GroupsApiTest : AbstractRestApiTest() {

    val rootUrl = "/v1/groups"

    @Autowired
    protected lateinit var groupService: GroupService

    @BeforeEach
    fun clearRepo() {

        mockUserAuthentication()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `should return all Groups where user is member of`() {

        val groups = this.performGet("$rootUrl/my", accessToken = accessToken.token)
            .expectOk()
            .document("groups-my-success", responseFields(groupFields("[].")))
            .returnsList(GroupDto::class.java)

        assertEquals(1, groups.size)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `should return all Groups where user is member of filtered by person`() {

        val person = account.persons.first()

        val groups = this.performGet("$rootUrl/filtered?personId=${person.id}", accessToken = accessToken.token)
            .expectOk()
            .document(
                "groups-filtered-success",
                requestParameters(
                    param("personId", "UUID of Person of own user").optional(),
                    param("name", "name (fragment) of an accessible group").optional()
                ),
                responseFields(groupFields("[]."))
            )
            .returnsList(GroupDto::class.java)

        assertEquals(1, groups.size)
        assertEquals(group.id, groups.first().id)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `should return a specific Group which user belongs to`() {
        val mockGroup = groupService.update(createMockGroup().add(account.person()))

        val group = this.performGet("$rootUrl/${mockGroup.id}", accessToken = accessToken.token)
            .expectOk()
            .document("groups-one-success", responseFields(groupFields()))
            .returns(GroupDto::class.java)

        assertEquals(mockGroup.id, group.id)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `must not return groups without valid accessToken`() {
        forbiddenGET("$rootUrl/my")
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `must not return any group without valid accessToken`() {
        forbiddenGET("$rootUrl/${group.id}")
    }

    private fun groupFields(prefix: String = ""): List<FieldDescriptor> {
        return listOf(
            field(prefix + "id", STRING, "UUID"),
            field(prefix + "name", STRING, "A notnull name describing this group"),
            field(prefix + "members", ARRAY, "All persons who are members of this group"),

            ).toMutableList().apply {
            addAll(personFields(prefix + "members[]."))
        }
    }
}
