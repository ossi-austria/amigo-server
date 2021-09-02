package org.ossiaustria.amigo.platform.rest.v1.user


import io.micrometer.core.annotation.Timed
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.services.GroupService
import org.ossiaustria.amigo.platform.rest.CurrentUserService
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

