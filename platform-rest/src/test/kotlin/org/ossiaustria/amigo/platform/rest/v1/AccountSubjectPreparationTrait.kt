package org.ossiaustria.amigo.platform.rest.v1

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.repositories.AccountRepository
import org.ossiaustria.amigo.platform.repositories.GroupRepository
import org.ossiaustria.amigo.platform.repositories.PersonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID
import javax.transaction.Transactional

@Component
internal class AccountSubjectPreparationTrait {

    lateinit var group: Group
    lateinit var account: Account
    lateinit var account2: Account
    lateinit var subject: Person
    lateinit var subject2: Person

    @Autowired
    protected lateinit var personRepository: PersonRepository

    @Autowired
    protected lateinit var groupRepository: GroupRepository

    @Autowired
    protected lateinit var accountRepository: AccountRepository

    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()

    fun apply() {
        deleteAll()
        applyAccount()
    }

    fun applyAccount() {
        group = createMockGroup()
        account = createMockAccount()
        account2 = createMockAccount(userOverrideSuffix = "0002")
        subject = account.persons.first()
        subject2 = account2.persons.first()
    }

    fun deleteAll() {
        personRepository.deleteAll()
        accountRepository.deleteAll()
        groupRepository.deleteAll()
    }

    @Transactional
    fun createMockGroup(): Group = groupRepository.save(
        Group(
            id = randomUUID(),
            name = "test group"
        )
    )

    @Transactional
    fun createMockAccount(
        plainPassword: String = "password",
        userOverrideSuffix: String? = null,
    ): Account {

        val accountId = randomUUID()
        var userSuffix = "100" + accountRepository.count()
        if (userOverrideSuffix != null) userSuffix = userOverrideSuffix

        val passwordEncrypted = passwordEncoder.encode(plainPassword)

        val account = accountRepository.save(
            Account(
                id = accountId,
                email = "email$userSuffix@example.com",
                passwordEncrypted = passwordEncrypted,
                persons = listOf(
                    Person(
                        id = randomUUID(),
                        accountId = accountId,
                        name = "user name $userSuffix",
                        groupId = group.id
                    )
                )
            )
        )



        return accountRepository.findOneByEmail(account.email)!!
    }
}