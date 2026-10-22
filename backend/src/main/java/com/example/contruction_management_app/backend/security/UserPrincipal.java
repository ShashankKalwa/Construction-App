package com.example.contruction_management_app.backend.security;

import com.example.contruction_management_app.backend.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final String id;
    private final String email;
    private final String passwordHash;
    private final String organizationId;
    private final String roleId;
    private final Collection<GrantedAuthority> authorities;
    private final boolean active;

    public UserPrincipal(String id, String email, String passwordHash, String organizationId, String roleId, Collection<GrantedAuthority> authorities, boolean active) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.organizationId = organizationId;
        this.roleId = roleId;
        this.authorities = authorities;
        this.active = active;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        // Add ROLE_ authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getId().toUpperCase()));
        // Add granular permissions
        if (user.getRole().getPermissions() != null) {
            user.getRole().getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getId()))
            );
        }

        return new UserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getHashedPassword(),
            user.getOrganization().getId(),
            user.getRole().getId(),
            authorities,
            user.isActive()
        );
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getOrganizationId() { return organizationId; }
    public String getRoleId() { return roleId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}
