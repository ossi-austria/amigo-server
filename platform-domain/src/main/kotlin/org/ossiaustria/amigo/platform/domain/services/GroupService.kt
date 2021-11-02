package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.repositories.GroupRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID
import java.util.UUID.randomUUID
import javax.transaction.Transactional

sealed class GroupServiceError(errorName: String, message: String) : ServiceError(errorName, message, null) {

    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    class GroupNotFound(info: String) :
        ServiceError("GROUP_NOT_FOUND", "Group could not be found: $info")

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    class GroupMemberNotFound(info: String) :
        ServiceError("GROUP_MEMBER_NOT_FOUND", "Person could not be found in Group: $info")


    @ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED)
    class GroupOwnerNotChangeable :
        ServiceError("GROUP_OWNER_NOT_CHANGEABLE", "Removal of Group owner not supported")
}

interface GroupService {
    fun findGroupsOfUser(account: Account): List<Group>
    fun findGroup(account: Account, groupId: UUID): Group
    fun filterGroups(account: Account, personId: UUID? = null, name: String? = null): List<Group>
    fun createGroup(account: Account, name: String, adminName: String? = null): Group
    fun changeName(admin: Person, group: Group, name: String): Group
    fun findById(id: UUID): Group?
    fun count(): Long
    fun addMember(
        admin: Person,
        group: Group,
        accountMail: String,
        memberName: String,
        membershipType: MembershipType
    ): Group

    fun changeMember(admin: Person, group: Group, member: Person, membershipType: MembershipType): Group
    fun removeMember(admin: Person, group: Group, member: Person): Group
}

@Service
class GroupServiceImpl : SecuredService(), GroupService {

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    override fun findById(id: UUID) = groupRepository.findByIdOrNull(id)
    override fun count() = groupRepository.count()


    @Transactional
    override fun createGroup(account: Account, name: String, adminName: String?): Group {
        val groupId = randomUUID()
        val personForGroup = createPersonForGroup(account, groupId, adminName ?: account.email, MembershipType.OWNER)
        val group = groupRepository.save(
            Group(id = groupId, name = name, members = arrayListOf(personForGroup))
        )

        return groupRepository.findByIdOrNull(group.id)!!
    }

    @Transactional
    override fun changeName(admin: Person, group: Group, name: String): Group {
        assertPermission(admin, group, MembershipType.ADMIN)
        val existing = groupRepository.findByIdOrNull(group.id) ?: throw GroupServiceError.GroupNotFound(group.name)
        return groupRepository.save(existing.copy(name = name))
    }

    @Transactional
    override fun addMember(
        admin: Person,
        group: Group,
        accountMail: String,
        memberName: String,
        membershipType: MembershipType
    ): Group {
        assertPermission(admin, group, MembershipType.ADMIN)
        val existing = groupRepository.findByIdOrNull(group.id) ?: throw GroupServiceError.GroupNotFound(group.name)

        val account = accountRepository.findOneByEmail(accountMail) ?: throw SecurityError.PersonNotFound(accountMail)
        val element = Person(
            id = randomUUID(),
            groupId = existing.id,
            accountId = account.id,
            name = memberName,
            memberType = membershipType
        )
        return groupRepository.save(existing.add(element))
    }

    @Transactional
    override fun changeMember(admin: Person, group: Group, member: Person, membershipType: MembershipType): Group {
        assertPermission(admin, group, MembershipType.ADMIN)
        if (member.memberType == MembershipType.OWNER) throw GroupServiceError.GroupOwnerNotChangeable()
        if (membershipType == MembershipType.OWNER) throw GroupServiceError.GroupOwnerNotChangeable()
        if (group.findMember(member) == null) throw GroupServiceError.GroupMemberNotFound(member.name)
        // violates the DDD principle a bit
        personRepository.save(member.copy(memberType = membershipType))
        return groupRepository.findByIdOrNull(group.id)!!
    }

    @Transactional
    override fun removeMember(admin: Person, group: Group, member: Person): Group {
        assertPermission(admin, group, MembershipType.ADMIN)
        if (member.memberType == MembershipType.OWNER) throw GroupServiceError.GroupOwnerNotChangeable()
        if (group.findMember(member) == null) throw GroupServiceError.GroupMemberNotFound(member.name)
        return groupRepository.save(group.removeMember(member))
    }

    @Transactional
    override fun findGroupsOfUser(account: Account): List<Group> {
        val ids = groupIdsOfAccount(account)
        return groupRepository.findByIdIn(ids)
    }

    @Transactional
    override fun findGroup(account: Account, groupId: UUID): Group {
        val ids = groupIdsOfAccount(account)
        val group = if (ids.contains(groupId)) {
            groupRepository.findByIdOrNull(groupId) ?: throw GroupServiceError.GroupNotFound(groupId.toString())
        } else {
            log.warn("User requested group, which they are not a member of: ${account.email}: $groupId")
            throw NotFoundException(ErrorCode.NotFound, "Cannot find Group with id $groupId")
        }
        group.members.size
        return group
    }

    @Transactional
    override fun filterGroups(account: Account, personId: UUID?, name: String?): List<Group> {
        return arrayListOf<Group>().apply {
            if (personId != null) {
                account.persons
                    .filter { it.id == personId }
                    .map { it.groupId }
                    .firstOrNull()
                    ?.let { addAll(listOfNotNull(groupRepository.findByIdOrNull(it))) }
            }
            if (name != null) {
                val ids = account.persons.map { it.groupId }
                addAll(groupRepository.findByIdIn(ids).filter { it.name == name })
            }
        }
    }

    private fun createPersonForGroup(
        account: Account,
        groupId: UUID,
        fullName: String,
        memberType: MembershipType
    ): Person = Person(
        id = randomUUID(),
        accountId = account.id,
        name = fullName,
        groupId = groupId,
        memberType = memberType
    )

    private fun groupIdsOfAccount(account: Account) = account.persons.map { it.groupId }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
