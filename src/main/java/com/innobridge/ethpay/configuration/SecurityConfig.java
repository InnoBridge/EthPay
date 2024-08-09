package com.innobridge.ethpay.configuration;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.innobridge.ethpay.service.CustomAuthenticationProvider;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

  public static final String[] WHITELISTED_ENDPOINTS = {
          "/auth/signup",       // Signup endpoint
          "/swagger-ui/**",         // Swagger UI
          "/v3/api-docs/**",         // Swagger API docs
          "/oauth2/**"              // OAuth2 endpoints
  };

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
                                          CustomAuthenticationProvider authenticationProvider) throws Exception {
//      http.httpBasic(withDefaults());
//      http.authenticationProvider(authenticationProvider);
    http
            .csrf(csrf -> csrf.disable())  // Disable CSRF protection
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(WHITELISTED_ENDPOINTS).permitAll()  // Whitelist signup endpoint
                    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll() // Whitelist login POST endpoint
                    .anyRequest().authenticated()  // All other endpoints require authentication
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Stateless session management
            );
    return http.build();
  }

  @Bean
  CustomAuthenticationProvider customAuthenticationProvider(UserDetailsService userDetailsService) {
    return new CustomAuthenticationProvider(userDetailsService);
  }

  @Bean
  UserDetailsService userDetailsService() {
    UserDetails user = User.withUsername("user")
        .password("password")
        .authorities("read")
        .roles("USER")
        .build();

    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
