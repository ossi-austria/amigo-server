package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Group
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

//interface PasswordService {
//    fun resetPasswordStart(email: String? = null, userName: String? = null, userId: UUID? = null)
//    fun passwordResetConfirm(token: String, password: String): Boolean
//}

@Service
class PersonService {

    @Autowired
    private lateinit var personRepository: PersonRepository

    fun findById(id: UUID) = personRepository.findByIdOrNull(id)


    fun createPersonForGroup(account: Account, group: Group, fullName: String): Person {
        return personRepository.save(
            Person(
                id = UUID.randomUUID(),
                accountId = account.id,
                name = fullName,
                groupId = group.id
            )
        )
    }

}