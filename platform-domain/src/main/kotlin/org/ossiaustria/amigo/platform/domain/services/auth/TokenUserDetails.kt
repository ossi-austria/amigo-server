package org.ossiaustria.amigo.platform.domain.services.auth

import org.ossiaustria.amigo.platform.domain.config.Constants
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class TokenUserDetails(
    val accountId: UUID,
    val email: String,
    @Deprecated("use given personId")
    val personsIds: List<UUID> = listOf(),
    val expiration: Date = Date(),
    val issuedAt: Date = Date(),
    val issuer: String? = Constants.JWT_ISSUER,
) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = arrayListOf<GrantedAuthority>(
        SimpleGrantedAuthority("ROLE_USER")
    )

    override fun getPassword(): String = TODO("Not yet implemented")

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = expiration.after(Date())

    override fun isEnabled(): Boolean = true

    @Deprecated("use given personID")
    fun personId() = personsIds.first()

}