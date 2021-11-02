package org.ossiaustria.amigo.platform.rest.v1

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.services.GroupService
import org.ossiaustria.amigo.platform.rest.v1.user.AddGroupMemberRequest
import org.ossiaustria.amigo.platform.rest.v1.user.ChangeGroupMemberRequest
import org.ossiaustria.amigo.platform.rest.v1.user.ChangeGroupRequest
import org.ossiaustria.amigo.platform.rest.v1.user.CreateGroupRequest
import org.ossiaustria.amigo.platform.rest.v1.user.GroupDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType.ARRAY
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
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
    fun `createGroup should create a new group with creator as owner`() {

        val request = CreateGroupRequest(
            name = "new group",
            ownerName = "Owner"
        )
        val groups = this.performPost(rootUrl, accessToken = accessToken.token, body = request)
            .expectOk()
            .document(
                "groups-create", responseFields(groupFields()),
                requestFields(
                    arrayListOf(
                        field("name", STRING, "New name of Group"),
                        field("ownerName", STRING, "Initial name of Person created for Owner"),
                    )
                )
            )
            .returns(GroupDto::class.java)

        assertEquals("new group", groups.name)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `changeGroupName should create a new group with creator as owner`() {

        val mockGroup = createMockGroup().add(account.primaryPerson())

        val request = ChangeGroupRequest(
            name = "newGroupName",
        )
        val groups = this.performPatch("$rootUrl/${mockGroup.id}", accessToken = accessToken.token, body = request)
            .expectOk()
            .document(
                "groups-change",
                responseFields(groupFields()),
                requestFields(arrayListOf(field("name", STRING, "New name of Group")))
            )
            .returns(GroupDto::class.java)

        assertEquals("newGroupName", groups.name)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `addMember should add an new person to the group`() {

        val mockGroup = createMockGroup()

        val request = AddGroupMemberRequest(
            name = "newGroupName",
            email = account2.email,
            membershipType = MembershipType.ADMIN
        )
        val groupDto =
            this.performPost("$rootUrl/${mockGroup.id}/members", accessToken = accessToken.token, body = request)
                .expectOk()
                .document(
                    "groups-member-add", responseFields(groupFields()),
                    requestFields(
                        arrayListOf(
                            field("email", STRING, "Email of existing Account to create a new Person for this Group "),
                            field("name", STRING, "Name of new Person"),
                            field("membershipType", STRING, "membershipType, should be MEMBER, ANALOGUE or ADMIN "),
                        )
                    )
                ).returns(GroupDto::class.java)

        assertEquals(2, groupDto.members.size)
        assertEquals(MembershipType.ADMIN, groupDto.members.last().memberType)
        assertEquals("newGroupName", groupDto.members.last().name)
        assertEquals(groupDto.id, groupDto.members.last().groupId)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `changeMember should change membershipType`() {

        val mockGroup = createMockGroup()
        val group =
            groupService.addMember(mockGroup.owner(), mockGroup, account2.email, "mmeber2", MembershipType.MEMBER)

        val personId = group.members.last().id

        val request = ChangeGroupMemberRequest(
            membershipType = MembershipType.ADMIN
        )
        val groupDto =
            this.performPatch(
                "$rootUrl/${mockGroup.id}/members/$personId",
                accessToken = accessToken.token,
                body = request
            )
                .expectOk()
                .document(
                    "groups-member-change", responseFields(groupFields()),
                    requestFields(
                        arrayListOf(
                            field("membershipType", STRING, "membershipType, should be MEMBER, ANALOGUE or ADMIN "),
                        )
                    )
                )
                .returns(GroupDto::class.java)

        assertEquals(2, groupDto.members.size)
        assertEquals(MembershipType.ADMIN, groupDto.members.last().memberType)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `removeMember should remove existing Group member`() {

        val mockGroup = createMockGroup()
        val group =
            groupService.addMember(mockGroup.owner(), mockGroup, account2.email, "member2", MembershipType.MEMBER)

        val personId = group.members.last().id

        this.performDelete("$rootUrl/${mockGroup.id}/members/$personId", accessToken = accessToken.token)
            .expectNoContent()
            .document("groups-member-remove", responseFields(groupFields()))

        val groupDto = this.performGet("$rootUrl/${mockGroup.id}", accessToken = accessToken.token)
            .expectOk()
            .returns(GroupDto::class.java)

        assertEquals(1, groupDto.members.size)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `myGroups should return all Groups where user is member of`() {

        val groups = this.performGet(rootUrl, accessToken = accessToken.token, person1Id)
            .expectOk()
            .document(
                "groups-my-success",
                responseFields(groupFields("[]."))
            )
            .returnsList(GroupDto::class.java)

        assertEquals(2, groups.size)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `myGroups needs authentication`() {
        val url = "$rootUrl/my"
        this.performPost(url).expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `filtered should return all Groups where user is member of filtered by person`() {

        val person = account.persons.first()

        val groups = this.performGet(
            "$rootUrl/filtered?personId=${person.id}",
            accessToken = accessToken.token
        )
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
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `filtered needs authentication`() {
        val person = account.persons.first()
        this.performPost("$rootUrl/filtered").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `getGroup should return a specific Group which user belongs to`() {
        val mockGroup = createMockGroup().add(account.primaryPerson())

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
