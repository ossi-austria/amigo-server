package org.ossiaustria.amigo.platform.rest.v1.user


import io.micrometer.core.annotation.Timed
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
@RequestMapping("/v1/groups", produces = ["application/json"], consumes = ["application/json"])
class GroupsApi(
    val groupsService: GroupService,
    val currentUserService: CurrentUserService
) {

    @GetMapping("/my")
    fun getMyGroups(account: Account): List<GroupDto> =
        groupsService.findGroupsOfUser(account).map(Group::toDto)

    @PostMapping()
    fun createGroup(
        account: Account,
        @RequestBody createGroupRequest: CreateGroupRequest,
    ): GroupDto {
        return groupsService.createGroup(
            account,
            createGroupRequest.name,
            createGroupRequest.ownerName
        ).toDto()
    }


    @PatchMapping("/{id}")
    fun changeGroupName(
        account: Account,
        @PathVariable("id") id: UUID,
        @RequestBody changeGroupRequest: ChangeGroupRequest
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

    @PostMapping("/{id}/members")
    fun addMember(
        account: Account,
        @PathVariable("id") id: UUID,
        @RequestBody request: AddGroupMemberRequest
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

    @PatchMapping("/{id}/members/{personId}")
    fun changeMember(
        account: Account,
        @PathVariable("id") id: UUID,
        @PathVariable("personId") personId: UUID,
        @RequestBody request: ChangeGroupMemberRequest
    ): GroupDto {
        val findGroup = groupsService.findGroup(account, id)
        val admin = findGroup.findAdmin(account)
            ?: throw SecurityError.PersonHasInsufficientRights(MembershipType.ADMIN)
        val member = findGroup.findMember(personId)
            ?: throw SecurityError.PersonNotFound(personId.toString())
        return groupsService.changeMember(admin, findGroup, member, request.membershipType).toDto()
    }

    @DeleteMapping("/{id}/members/{personId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeMember(
        account: Account,
        @PathVariable("id") id: UUID,
        @PathVariable("personId") personId: UUID,
    ): GroupDto {
        val findGroup = groupsService.findGroup(account, id)
        val admin = findGroup.findAdmin(account)
            ?: throw SecurityError.PersonHasInsufficientRights(MembershipType.ADMIN)
        val member = findGroup.findMember(personId)
            ?: throw SecurityError.PersonNotFound(personId.toString())
        return groupsService.removeMember(admin, findGroup, member).toDto()
    }

    @GetMapping("/{id}")
    fun getGroup(
        @PathVariable("id") id: UUID,
        account: Account
    ): GroupDto =
        groupsService.findGroup(account, id).toDto()


    @GetMapping("/filtered")
    fun filterGroups(
        @RequestParam(value = "personId", required = false) personId: UUID?,
        @RequestParam(value = "name", required = false) name: String?,
        account: Account
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

