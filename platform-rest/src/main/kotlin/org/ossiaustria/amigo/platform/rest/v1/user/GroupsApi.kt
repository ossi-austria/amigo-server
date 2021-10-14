package org.ossiaustria.amigo.platform.rest.v1.user


import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.services.GroupService
import org.ossiaustria.amigo.platform.domain.services.SecurityError
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Timed(value = "time.api.groups")
@RestController
@RequestMapping("/v1/groups", produces = ["application/json"])
class GroupsApi(
    val groupsService: GroupService,
    val currentUserService: CurrentUserService
) {

    @ApiOperation("Get own Group[s]")
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

    @ApiOperation("Add a new Person to this Group")
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

    @ApiOperation("Change a Person/Membership of this Group")
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

    @ApiOperation("Remove a Person/Membership of this Group")
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

    @ApiOperation("Search/filter visible Groups")
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

