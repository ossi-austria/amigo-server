package org.ossiaustria.amigo.platform.domain.services


import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.repositories.GroupRepository
import org.ossiaustria.amigo.platform.domain.testcommons.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.annotation.Rollback
import java.util.UUID
import java.util.UUID.randomUUID
import javax.transaction.Transactional

internal class GroupServiceImplTest : AbstractServiceTest() {

    @Autowired
    private lateinit var groupService: GroupService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var groupRepository: GroupRepository

    private lateinit var account: Account
    private lateinit var group1: Group
    private lateinit var group2: Group

    @BeforeEach
    fun setup() {
        accountRepository.deleteAll()
        groupRepository.deleteAll()
        accountRepository.deleteAll()
        val (account1, group1, group2) = createAccountWithGroups()
        account = account1
        this.group1 = group1
        this.group2 = group2
    }


    @Test
    fun `findGroup should find any group which contains user's account`() {
        assertNotNull(groupService.findGroup(account, group1.id))
        assertNotNull(groupService.findGroup(account, group2.id))
    }

    @Test
    fun `findGroup should contain all its members in list`() {
        val findGroup = groupService.findGroup(account, group1.id)
        then(findGroup, notNullValue())
        then(findGroup.members, notNullValue())
        then(findGroup.members, hasSize(1))
        then(findGroup.members.first(), notNullValue())
    }

    @Test
    fun `findGroup must not find group which does not contain user's account`() {
        val (_, group2) = createAccountWithGroups("user2@email.com")

        assertThrows(GroupServiceError.GroupNotFound::class.java) {
            groupService.findGroup(account, group2.id)
        }
    }

    @Transactional
    @Rollback
    @Test
    fun `findGroupsOfUser should find Groups Of User which does contain user's account`() {

        val test = groupService.findGroupsOfUser(account)

        assertEquals(2, test.size)
        assertTrue(test.containsAll(listOf(group1, group2)))
    }

    @Transactional
    @Rollback
    @Test
    fun `findGroupsOfUser must not find Groups Of User which does not contain user's account`() {
        val account = accountRepository.save(
            Account(
                id = randomUUID(),
                email = "email",
                passwordEncrypted = "asd",
                persons = listOf()
            )
        )
        val test = groupService.findGroupsOfUser(account)

        assertEquals(0, test.size)
    }

    @Test
    fun `filterGroups should return empty list for account with no groups`() {
        val account = accountRepository.save(
            Account(
                id = randomUUID(),
                email = "email",
                passwordEncrypted = "asd",
                persons = listOf()
            )
        )
        val result = groupService.filterGroups(account)
        then(result, empty())
    }

    @Test
    fun `filterGroups should return list with group name in user's group`() {
        val result = groupService.filterGroups(account, name = group1.name)
        then(result, hasSize(1))
        then(result.first().name, equalTo(group1.name))
    }

    @Test
    fun `filterGroups should return list with personId in user's group`() {
        val result = groupService.filterGroups(account, personId = account.persons.first().id)
        then(result, hasSize(1))
        then(result.first().name, equalTo(group1.name))
    }

    @Test
    fun `filterGroups must not return list with group name in user's group`() {
        val (_, group3, _) = createAccountWithGroups("user2@email.org")
        val result = groupService.filterGroups(account, name = group3.name)
        then(result, empty())
    }

    @Test
    fun `createGroup should return new group with name`() {
        val (account, _, _) = createAccountWithGroups("user2@email.org")
        val result = groupService.createGroup(account, "groupName", "admin")
        then(result.name, equalTo("groupName"))
    }

    @Test
    fun `createGroup should return new group with new person for account as owner`() {
        val (account, _, _) = createAccountWithGroups("user2@email.org")
        val result = groupService.createGroup(account, "groupName", "admin")
        then(result.members, hasSize(1))
        val actual = result.owner().accountId
        then(actual, equalTo(account.id))
    }

    @Test
    fun `changeName should change name when person is OWNER`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.OWNER)

        val result = groupService.changeName(account.primaryPerson(), group, "groupName")
        then(result.name, equalTo("groupName"))
    }

    @Test
    fun `changeName should change name when person is ADMIN`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.ADMIN)
        val result = groupService.changeName(account.primaryPerson(), group, "groupName")
        then(result.name, equalTo("groupName"))
    }

    @Test
    fun `changeName must not change name when person is MEMBER`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org")

        assertThrows(SecurityError.PersonHasInsufficientRights::class.java) {
            groupService.changeName(account.primaryPerson(), group, "groupName")
        }
    }

    @Test
    fun `addMember must not add when actor is not admin`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org")
        val (account2, _, _) = createAccountWithGroups("user3@email.org")

        assertThrows(SecurityError.PersonHasInsufficientRights::class.java) {
            groupService.addMember(account.primaryPerson(), group, account2.email, "new member", MembershipType.MEMBER)
        }
    }

    @Test
    fun `addMember must not add when account does not exist`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.ADMIN)

        assertThrows(SecurityError.PersonNotFound::class.java) {
            groupService.addMember(account.primaryPerson(), group, "random@email", "new member", MembershipType.MEMBER)
        }
    }

    @Test
    fun `addMember should add new member with correct name and type`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.ADMIN)
        val (account2, _, _) = createAccountWithGroups("user3@email.org")

        val result =
            groupService.addMember(
                account.primaryPerson(),
                group,
                account2.email,
                "new member",
                MembershipType.ANALOGUE
            )
        then(result.members, hasSize(2))
        then(result.members.last().accountId, equalTo(account2.id))
        then(result.members.last().name, equalTo("new member"))
        then(result.members.last().memberType, equalTo(MembershipType.ANALOGUE))
    }

    @Test
    fun `changeMember should add change membershipType for existing non-Owner users`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.ADMIN)
        val (account2, _, _) = createAccountWithGroups("user3@email.org")
        val groupAddedMember =
            groupService.addMember(
                account.primaryPerson(),
                group,
                account2.email,
                "new member",
                MembershipType.ANALOGUE
            )
        then(groupAddedMember.members, hasSize(2))
        val member = groupAddedMember.members.last()
        val result =
            groupService.changeMember(account.primaryPerson(), groupAddedMember, member, MembershipType.ADMIN)
        then(result.members, hasSize(2))
        then(result.members.last().accountId, equalTo(account2.id))
        then(result.members.last().id, equalTo(member.id))
        then(result.members.last().name, equalTo("new member"))
        then(result.members.last().memberType, equalTo(MembershipType.ADMIN))
    }

    @Test
    fun `changeMember fails for non-existing members`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.OWNER)
        val (account2, _, _) = createAccountWithGroups("user3@email.org")

        assertThrows(GroupServiceError.GroupMemberNotFound::class.java) {
            groupService.changeMember(account.primaryPerson(), group, account2.primaryPerson(), MembershipType.ADMIN)
        }
    }

    @Test
    fun `changeMember fails for change of original Owner`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.OWNER)
        val (_, _, _) = createAccountWithGroups("user3@email.org")

        assertThrows(GroupServiceError.GroupOwnerNotChangeable::class.java) {
            groupService.changeMember(account.primaryPerson(), group, account.primaryPerson(), MembershipType.ADMIN)
        }
    }

    @Test
    fun `changeMember fails for change of secondary Owner`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.OWNER)
        val (account2, _, _) = createAccountWithGroups("user3@email.org")
        val addMember =
            groupService.addMember(account.primaryPerson(), group, account2.email, "new member", MembershipType.OWNER)

        assertThrows(GroupServiceError.GroupOwnerNotChangeable::class.java) {
            groupService.changeMember(account.primaryPerson(), group, addMember.members.last(), MembershipType.ADMIN)
        }
    }

    @Test
    fun `removeMember should remove member for existing non-Owner users`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.ADMIN)
        val (account2, _, _) = createAccountWithGroups("user3@email.org")
        val groupAfterAdding =
            groupService.addMember(
                account.primaryPerson(),
                group,
                account2.email,
                "new member",
                MembershipType.ANALOGUE
            )
        then(groupAfterAdding.members, hasSize(2))
        val addedMember = groupAfterAdding.members.last()
        val result = groupService.removeMember(account.primaryPerson(), groupAfterAdding, addedMember)
        then(result.findMember(addedMember), equalTo(null))
    }

    @Test
    fun `removeMember fails for removal of secondary Owner`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.ADMIN)
        val (account2, _, _) = createAccountWithGroups("user3@email.org")
        val addMember =
            groupService.addMember(account.primaryPerson(), group, account2.email, "new member", MembershipType.OWNER)

        assertThrows(GroupServiceError.GroupOwnerNotChangeable::class.java) {
            groupService.removeMember(account.primaryPerson(), group, addMember.members.last())
        }
    }

    @Test
    fun `removeMember fails for removal of original Owner`() {
        val (account, group, _) = createAccountWithGroups("user2@email.org", MembershipType.OWNER)
        val (account2, _, _) = createAccountWithGroups("user3@email.org")
        groupService.addMember(account.primaryPerson(), group, account2.email, "new member", MembershipType.OWNER)

        assertThrows(GroupServiceError.GroupOwnerNotChangeable::class.java) {
            groupService.removeMember(account.primaryPerson(), group, account.primaryPerson())
        }
    }

    @Test
    fun `filterGroups must not return list with personId id not in user's group`() {
        val (account2, _, _) = createAccountWithGroups("user2@email.org")
        val result = groupService.filterGroups(account, personId = account2.persons.first().id)
        then(result, empty())
    }

    @Transactional
    fun createAccountWithGroups(
        email: String = "user1@email.org",
        type: MembershipType = MembershipType.MEMBER
    ): Triple<Account, Group, Group> {
        val accountId = randomUUID()
        val groupId1 = randomUUID()
        val groupId2 = randomUUID()
        val group1 = groupRepository.save(Group(groupId1, "$email groupId1"))
        val group2 = groupRepository.save(Group(groupId2, "$email groupId2"))
        val person1 = createPerson(accountId, email, groupId1, type)
        val person2 = createPerson(accountId, email, groupId2, type)

        accountRepository.save(
            Account(
                id = accountId,
                email = email,
                passwordEncrypted = "asd",
                persons = listOf(person1, person2)
            )
        )

        return Triple(
            accountRepository.findByIdOrNull(accountId)!!,
            groupRepository.findByIdOrNull(group1.id)!!,
            groupRepository.findByIdOrNull(group2.id)!!,
        )
    }

    private fun createPerson(
        accountId: UUID,
        email: String,
        groupId: UUID,
        type: MembershipType = MembershipType.MEMBER
    ) = Person(
        randomUUID(),
        accountId,
        email,
        groupId,
        type
    )
}
