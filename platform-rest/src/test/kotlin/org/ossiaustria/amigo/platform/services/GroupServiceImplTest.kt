package org.ossiaustria.amigo.platform.services


import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.ossiaustria.amigo.platform.repositories.AccountRepository
import org.ossiaustria.amigo.platform.repositories.GroupRepository
import org.ossiaustria.amigo.platform.service.AbstractServiceTest
import org.ossiaustria.amigo.platform.testcommons.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.annotation.Rollback
import java.util.*
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

        assertThrows(NotFoundException::class.java) {
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
    fun `filterGroups must not return list with personId id not in user's group`() {
        val (account2, _, _) = createAccountWithGroups("user2@email.org")
        val result = groupService.filterGroups(account, personId = account2.persons.first().id)
        then(result, empty())
    }

    @Transactional
    fun createAccountWithGroups(email: String = "user1@email.org"): Triple<Account, Group, Group> {
        val accountId = randomUUID()
        val groupId1 = randomUUID()
        val groupId2 = randomUUID()
        val group1 = groupRepository.save(Group(groupId1, "$email groupId1"))
        val group2 = groupRepository.save(Group(groupId2, "$email groupId2"))
        val person1 = createPerson(accountId, email, groupId1)
        val person2 = createPerson(accountId, email, groupId2)
        val account = accountRepository.save(
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
        groupId: UUID
    ) = Person(
        randomUUID(),
        accountId,
        email,
        groupId,
        MembershipType.MEMBER
    )
}