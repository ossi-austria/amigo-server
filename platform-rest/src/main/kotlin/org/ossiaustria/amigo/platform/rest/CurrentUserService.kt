package org.ossiaustria.amigo.platform.rest

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.repositories.AccountRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.exceptions.UserNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

interface CurrentUserService {
    fun authentication(): Authentication
    fun authenticationOrNull(): Authentication?
    fun person(): Person
    fun account(): Account
}

@Component
class SimpleCurrentUserService(
    val accountRepository: AccountRepository,
    val personRepository: PersonRepository
) : CurrentUserService {

    override fun authentication(): Authentication {
        return SecurityContextHolder.getContext().authentication
    }

    override fun authenticationOrNull(): Authentication? {
        return SecurityContextHolder.getContext().authentication
    }

    override fun person(): Person {
        val tokenUserDetails: TokenUserDetails = authentication().principal as TokenUserDetails
        return personRepository.findByIdOrNull(tokenUserDetails.accountId)
            ?: throw UserNotFoundException()
    }

    override fun account(): Account {
        val tokenUserDetails: TokenUserDetails = authentication().principal as TokenUserDetails
        return accountRepository.findByIdOrNull(tokenUserDetails.accountId)
            ?: throw UserNotFoundException()
    }

}
