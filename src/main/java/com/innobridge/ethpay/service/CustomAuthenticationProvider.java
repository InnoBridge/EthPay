package com.innobridge.ethpay.service;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

  UserDetailsService userDetailsService;

  public CustomAuthenticationProvider(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String username = authentication.getName();
    String password = authentication.getCredentials().toString();

    UserDetails user = userDetailsService.loadUserByUsername(username);

    if (user != null && user.getUsername().equals(username) && user.getPassword().equals(password)) {
      return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
    } else {
      throw new BadCredentialsException("Authentication failed");
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken
      .class
      .isAssignableFrom(authentication);
  }
}
