package com.innobridge.ethpay.model;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UsernameEmailPasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

    @Getter
    private final String id;
    private final Boolean withUsername;

    public UsernameEmailPasswordAuthenticationToken(Object principal, Object credentials, boolean withUsername) {
        super(principal, credentials);
        this.id = null;
        this.withUsername = withUsername;
    }

    public UsernameEmailPasswordAuthenticationToken(String id, Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(principal, null, authorities);
        this.id = id;
        this.withUsername = null;
    }

    public boolean isWithUsername() {
        return withUsername;
    }

}