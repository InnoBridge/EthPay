package com.innobridge.ethpay.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Bean
  UserDetailsService userDetailsService() {
    UserDetails user = User.withUsername("user")
        .password("{noop}password")
        .authorities("read")
        .roles("USER")
        .build();

    return new InMemoryUserDetailsManager(user);
  }

}
