package com.innobridge.ethpay.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      return http.build();
  }

  // @Bean
  // UserDetailsService userDetailsService() {
  //   UserDetails user = User.withUsername("user")
  //       .password("{noop}password")
  //       .authorities("read")
  //       .roles("USER")
  //       .build();
  //
  //   return new InMemoryUserDetailsManager(user);
  // }

}
