package org.ossiaustria.amigo.platform.rest.v1

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.enums.MembershipType
import org.ossiaustria.amigo.platform.domain.services.AccountService
import org.ossiaustria.amigo.platform.domain.services.GroupService
import org.ossiaustria.amigo.platform.domain.services.PersonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
internal class AccountSubjectPreparationTrait {

    var group: Group? = null
    lateinit var account: Account
    lateinit var account2: Account
    lateinit var person: Person
    lateinit var person2: Person

    @Autowired
    protected lateinit var groupService: GroupService

    @Autowired
    protected lateinit var personService: PersonService

    @Autowired
    protected lateinit var accountService: AccountService

    fun apply() {
        account = createMockAccount()
        group = createMockGroup(account)
        account = accountService.findOneByEmail(account.email)!! // has to be reloaded
        account2 = createMockAccount(userOverrideSuffix = "0002")
        person = account.persons.first()
        person2 = account2.persons.first()
    }

    fun createMockGroup(account: Account): Group {
        groupInvocations++
        return groupService.createGroup(account, "Group-$groupInvocations")
    }

    fun createMockAccount(
        plainPassword: String = "password",
        userOverrideSuffix: String = "",
    ): Account {
        accountInvocations++
        val userSuffix = "-$accountInvocations$userOverrideSuffix"

        val nextAccout = accountService.createAccount("email$userSuffix@example.com", plainPassword)

        group?.let {
            groupService.addMember(
                group!!.owner(),
                group!!,
                nextAccout.email,
                "user name $userSuffix",
                MembershipType.ADMIN
            )
        }

        return accountService.findOneByEmail(nextAccout.email)!!
    }

    companion object {
        private var groupInvocations: Int = 0
        private var accountInvocations: Int = 0
    }
}