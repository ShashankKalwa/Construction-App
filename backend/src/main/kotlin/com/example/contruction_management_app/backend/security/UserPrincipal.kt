package com.example.contruction_management_app.backend.security

import com.example.contruction_management_app.backend.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    val id: String,
    val email: String,
    private val passwordHash: String,
    val organizationId: String,
    val roleId: String,
    private val authorities: Collection<GrantedAuthority>,
    private val active: Boolean
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun getPassword(): String = passwordHash
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = active

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = mutableListOf<GrantedAuthority>()
            // Add ROLE_ authority
            authorities.add(SimpleGrantedAuthority("ROLE_${user.role.id.uppercase()}"))
            // Add granular permissions
            user.role.permissions.forEach { permission ->
                authorities.add(SimpleGrantedAuthority(permission.id))
            }

            return UserPrincipal(
                id = user.id,
                email = user.email,
                passwordHash = user.hashedPassword,
                organizationId = user.organization.id,
                roleId = user.role.id,
                authorities = authorities,
                active = user.isActive
            )
        }
    }
}
