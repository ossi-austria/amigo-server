package org.ossiaustria.amigo.platform.services.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class TokenUserDetails(
    val accountId: UUID,
    val email: String,
    val personsIds: List<UUID> = listOf(),
    val expiration: Date = Date(),
    val issuedAt: Date = Date(),
    val issuer: String? = "AMIGO-PLATFORM",
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

}