package com.innobridge.ethpay.security;

import com.innobridge.ethpay.model.UsernameEmailPasswordAuthenticationToken;
import com.innobridge.ethpay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsernameEmailPasswordAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernameEmailPasswordAuthenticationToken authRequest = (UsernameEmailPasswordAuthenticationToken) authentication;
        String principal = (String) authRequest.getPrincipal();
        String credentials = (String) authRequest.getCredentials();
        boolean withUsername = authRequest.isWithUsername();

        UserDetails user;

        if (withUsername) {
            user = userService.getByUsername(principal).orElseThrow(
                    () -> new AuthenticationServiceException("Invalid username/email or password"));
        } else {
            user = userService.getByEmail(principal).orElseThrow(
                    () -> new AuthenticationServiceException("Invalid username/email or password"));
        }

        if (!passwordEncoder.matches(credentials, user.getPassword())) {
            throw new AuthenticationServiceException("Invalid username/email or password");
        }

        return new UsernameEmailPasswordAuthenticationToken(user, credentials, withUsername, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernameEmailPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}