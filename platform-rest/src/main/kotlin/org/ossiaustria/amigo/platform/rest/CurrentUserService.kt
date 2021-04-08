package org.ossiaustria.amigo.platform.rest

import org.ossiaustria.amigo.platform.repositories.AccountRepository
import org.ossiaustria.amigo.platform.repositories.PersonRepository
import org.ossiaustria.amigo.platform.services.auth.TokenDetails
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.exceptions.UserNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

interface CurrentUserService {
    fun authentication(): Authentication
    fun authenticationOrNull(): Authentication?
    fun person(): Person
    fun personOrNull(): Person?
    fun account(): Account
    fun accountOrNull(): Account?
    fun accessToken(): String
    fun accessTokenOrNull(): String?
    fun anyValidToken(): String?
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
        val tokenDetails: TokenDetails = authentication().principal as TokenDetails
        return personRepository.findByIdOrNull(tokenDetails.personId)
            ?: throw UserNotFoundException()
    }

    override fun personOrNull(): Person? {
        val tokenDetails: TokenDetails? = authenticationOrNull()?.principal as? TokenDetails
        return tokenDetails?.let {
            personRepository.findByIdOrNull(tokenDetails.personId)
        }
    }

    override fun accessToken(): String {
        val tokenDetails: TokenDetails = authentication().principal as TokenDetails
        return tokenDetails.accessToken
    }

    override fun accessTokenOrNull(): String? {
        val tokenDetails: TokenDetails? = authenticationOrNull()?.principal as? TokenDetails
        return tokenDetails?.accessToken
    }

    override fun anyValidToken(): String? {
        return accessTokenOrNull()
    }

    override fun account(): Account {
        val tokenDetails: TokenDetails = authentication().principal as TokenDetails
        return accountRepository.findByIdOrNull(tokenDetails.accountId)
            ?: throw UserNotFoundException()
    }

    override fun accountOrNull(): Account? {
        val tokenDetails: TokenDetails? = authenticationOrNull()?.principal as? TokenDetails
        return tokenDetails?.let {
            accountRepository.findByIdOrNull(tokenDetails.accountId)
        }
    }

}
