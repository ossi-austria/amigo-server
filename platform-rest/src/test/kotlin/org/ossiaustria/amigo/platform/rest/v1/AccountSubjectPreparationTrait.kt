package org.ossiaustria.amigo.platform.rest.v1

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.services.AccountService
import org.ossiaustria.amigo.platform.domain.services.GroupService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID

@Component
internal class AccountSubjectPreparationTrait {

    lateinit var group: Group
    lateinit var account: Account
    lateinit var account2: Account
    lateinit var subject: Person
    lateinit var subject2: Person

    @Autowired
    protected lateinit var groupService: GroupService

    @Autowired
    protected lateinit var accountService: AccountService

    fun apply() {
        group = createMockGroup()
        account = createMockAccount()
        account2 = createMockAccount(userOverrideSuffix = "0002")
        subject = account.persons.first()
        subject2 = account2.persons.first()
    }


    fun createMockGroup(): Group {
        groupInvocations++
        val group = groupService.create(
            randomUUID(), "Group-$groupInvocations"
        )
        return groupService.update(group)
    }

    fun createMockAccount(
        plainPassword: String = "password",
        userOverrideSuffix: String = "",
    ): Account {
        accountInvocations++
        val userSuffix = "-$accountInvocations$userOverrideSuffix"

        val account = accountService.createAccountAndPerson(
            email = "email$userSuffix@example.com",
            plainPassword = plainPassword,
            group = group,
            fullName = "user name $userSuffix"
        )

        return accountService.findOneByEmail(account.email)!!
    }

    companion object {
        private var groupInvocations: Int = 0
        private var accountInvocations: Int = 0
    }
}