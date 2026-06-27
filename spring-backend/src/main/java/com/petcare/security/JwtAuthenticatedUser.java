package com.petcare.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class JwtAuthenticatedUser implements UserDetails {
    private final UUID userId;
    private final String username;
    private final String email;
    private final String rol;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtAuthenticatedUser(UUID userId, String username, String email, String rol, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.rol = rol;
        this.authorities = authorities;
    }

    public UUID getUserId() { return userId; }

    public String getEmail() { return email; }

    public String getRol() { return rol; }

    public String getRole() { return rol; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
