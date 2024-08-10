package com.innobridge.ethpay.model;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UsernameEmailPasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final boolean withUsername;

    public UsernameEmailPasswordAuthenticationToken(Object principal, Object credentials, boolean withUsername) {
        super(principal, credentials);
        this.withUsername = withUsername;
    }

    public UsernameEmailPasswordAuthenticationToken(Object principal, Object credentials, boolean withUsername, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.withUsername = withUsername;
    }

    public boolean isWithUsername() {
        return withUsername;
    }
}