package org.ossiaustria.amigo.platform.rest

import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.services.AccountService
import org.ossiaustria.amigo.platform.domain.services.PersonService
import org.ossiaustria.amigo.platform.domain.services.auth.TokenUserDetails
import org.ossiaustria.amigo.platform.exceptions.UserNotFoundException
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
    val accountRepository: AccountService,
    val personService: PersonService
) : CurrentUserService {

    override fun authentication(): Authentication {
        return SecurityContextHolder.getContext().authentication
    }

    override fun authenticationOrNull(): Authentication? {
        return SecurityContextHolder.getContext().authentication
    }

    override fun person(): Person {
        val tokenUserDetails: TokenUserDetails = authentication().principal as TokenUserDetails
        return personService.findById(tokenUserDetails.accountId)
            ?: throw UserNotFoundException()
    }

    override fun account(): Account {
        val tokenUserDetails: TokenUserDetails = authentication().principal as TokenUserDetails
        return accountRepository.findById(tokenUserDetails.accountId)
            ?: throw UserNotFoundException()
    }

}
