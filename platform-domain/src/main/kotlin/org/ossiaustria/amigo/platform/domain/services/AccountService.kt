package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

//interface PasswordService {
//    fun resetPasswordStart(email: String? = null, userName: String? = null, userId: UUID? = null)
//    fun passwordResetConfirm(token: String, password: String): Boolean
//}

@Service
class AccountService {

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    fun findById(id: UUID) = accountRepository.findByIdOrNull(id)

    fun findOneByEmail(email: String) = accountRepository.findOneByEmail(email)
    fun findOneByPersonId(id: UUID) = personRepository.findByIdOrNull(id)?.let {
        accountRepository.findByIdOrNull(it.accountId)
    }


    fun createAccount(email: String, plainPassword: String): Account {
        val passwordEncrypted = passwordEncoder.encode(plainPassword)
        val id = UUID.randomUUID()
        val account = Account(
            id = id,
            email = email,
            passwordEncrypted = passwordEncrypted,
        )
        return accountRepository.save(account)
    }



    fun requestPasswordChange(account: Account): Account {
        return accountRepository.save(
            account.copy(
                changeAccountToken = UUID.randomUUID().toString(),
                changeAccountTokenCreatedAt = ZonedDateTime.now()
            )
        )
    }

    fun count(): Long = accountRepository.count()
    fun findByIdOrNull(id: UUID): Account? = accountRepository.findByIdOrNull(id)


}