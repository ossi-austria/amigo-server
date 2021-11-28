package org.ossiaustria.amigo.platform.rest.v1.user


import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.services.GroupService
import org.ossiaustria.amigo.platform.domain.services.SecurityError
import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Timed(value = "amigo.api.groups")
@RestController
@RequestMapping("/v1/groups", produces = ["application/json"])
class GroupsApi(
    val groupsService: GroupService,
    val authService: AuthService,
    val currentUserService: CurrentUserService
) {

    @ApiOperation(
        "Get own Group[s]", notes = """
        Groups contain all necessary Person profiles inside them.
        A User can just access the Groups where they have a Person profile and is at least MEMBER (default).
        
        A Group can contain at max 1 ANALOGUE Person."""
    )
    @GetMapping
    fun getMyGroups(
        @ApiParam(hidden = true)
        account: Account
    ): List<GroupDto> =
        groupsService.findGroupsOfUser(account).map(Group::toDto)

    @ApiOperation("Create new Group and an admin Person for own Account")
    @PostMapping
    fun createGroup(
        @ApiParam(hidden = true)
        account: Account,

        @RequestBody
        createGroupRequest: CreateGroupRequest,
    ): GroupDto {
        return groupsService.createGroup(
            account,
            createGroupRequest.name,
            createGroupRequest.ownerName
        ).toDto()
    }


    @ApiOperation("Change name of Group via PATCH")
    @PatchMapping("/{id}")
    fun changeGroupName(
        @ApiParam(hidden = true)
        account: Account,

        @PathVariable("id")
        id: UUID,

        @RequestBody
        changeGroupRequest: ChangeGroupRequest
    ): GroupDto {
        val group = groupsService.findGroup(account, id)
        val person = group.findAdmin(account)
            ?: throw SecurityError.PersonHasInsufficientRights(MembershipType.ADMIN)
        return groupsService.changeName(
            person,
            group,
            changeGroupRequest.name,
        ).toDto()
    }

    @ApiOperation(
        "Add a new Person to this Group", notes = """
        *Email* is used to find an existing Account and create a new Person in this Group.
        *Name* and *MembershipType* must be initialised
    """
    )
    @PostMapping("/{id}/members")
    fun addMember(
        @ApiParam(hidden = true)
        account: Account,

        @PathVariable("id")
        id: UUID,

        @RequestBody
        request: AddGroupMemberRequest
    ): GroupDto {
        val group = groupsService.findGroup(account, id)
        val admin = group.findAdmin(account)
            ?: throw SecurityError.PersonHasInsufficientRights(MembershipType.ADMIN)
        return groupsService.addMember(
            admin,
            group,
            request.email,
            request.name,
            request.membershipType,
        ).toDto()
    }

    @ApiOperation(
        "Change a Person/Membership of this Group", notes = """
        Change privilege of a member.

        Attention: OWNER cannot be decreased in privilege.
    """
    )
    @PatchMapping("/{id}/members/{personId}")
    fun changeMember(
        @ApiParam(hidden = true)
        account: Account,

        @PathVariable("id")
        id: UUID,

        @PathVariable("personId")
        personId: UUID,

        @RequestBody
        request: ChangeGroupMemberRequest
    ): GroupDto {
        val findGroup = groupsService.findGroup(account, id)
        val admin = findGroup.findAdmin(account)
            ?: throw SecurityError.PersonHasInsufficientRights(MembershipType.ADMIN)
        val member = findGroup.findMember(personId)
            ?: throw SecurityError.PersonNotFound(personId.toString())
        return groupsService.changeMember(admin, findGroup, member, request.membershipType).toDto()
    }

    @ApiOperation(
        "Remove a Person/Member of this Group",
        notes = """
            Remove a non-OWNER of a Group.
            Note: This endpoint might change to not return a result"""
    )
    @DeleteMapping("/{id}/members/{personId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeMember(
        @ApiParam(hidden = true)
        account: Account,

        @PathVariable("id")
        id: UUID,

        @PathVariable("personId")
        personId: UUID,
    ): GroupDto {
        val findGroup = groupsService.findGroup(account, id)
        val admin = findGroup.findAdmin(account)
            ?: throw SecurityError.PersonHasInsufficientRights(MembershipType.ADMIN)
        val member = findGroup.findMember(personId)
            ?: throw SecurityError.PersonNotFound(personId.toString())
        return groupsService.removeMember(admin, findGroup, member).toDto()
    }

    @ApiOperation("Get a Group by :id")
    @GetMapping("/{id}")
    fun getGroup(
        @PathVariable("id")
        id: UUID,

        @ApiParam(hidden = true)
        account: Account
    ): GroupDto =
        groupsService.findGroup(account, id).toDto()

    @ApiOperation(
        "Search/filter visible Groups", notes = """
        Filter accessible Groups for *own* Person and/or Group name
    """
    )
    @GetMapping("/filtered")
    fun filterGroups(
        @RequestParam(value = "personId", required = false)
        personId: UUID?,

        @RequestParam(value = "name", required = false)
        name: String?,

        @ApiParam(hidden = true)
        account: Account,

        ): List<GroupDto> =
        groupsService.filterGroups(account, personId, name).map(Group::toDto)

}

data class CreateGroupRequest(
    val name: String,
    val ownerName: String? = null
)

data class ChangeGroupRequest(
    val name: String,
)

data class AddGroupMemberRequest(
    val email: String,
    val name: String,
    val membershipType: MembershipType
)

data class ChangeGroupMemberRequest(
    val membershipType: MembershipType
)

