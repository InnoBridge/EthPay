package com.innobridge.ethpay.configuration;

import com.innobridge.ethpay.security.JwtUtils;
import com.innobridge.ethpay.security.UsernameEmailPasswordAuthenticationProvider;
import com.innobridge.ethpay.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  public static final String[] WHITE_LIST_URL = {
          "/public/**",
          "/swagger-ui/**",         // Swagger UI
          "/v3/api-docs/**",         // Swagger API docs
          "/auth/signup",            // Signup endpoint
          "/auth/signin",            // Signin endpoint
          "/oauth2/**"               // OAuth2 endpoints
  };

  @Bean
  public AuthenticationProvider authenticationProvider() {
    return new UsernameEmailPasswordAuthenticationProvider();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtUtils jwtUtils(@Value("${JWT_ACCESS_SIGNING_KEY}") String accessSigningKey,
                           @Value("${JWT_REFRESH_SIGNING_KEY}") String refreshSigningKey,
                           UserService userService) {
    return new JwtUtils(accessSigningKey, refreshSigningKey, userService);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration authConfig) throws Exception {
    http
            .csrf(csrf -> csrf.disable())  // Disable CSRF protection
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(WHITE_LIST_URL).permitAll()  // Whitelist signup endpoint
                    .requestMatchers(HttpMethod.POST, "/auth/signin").permitAll() // Whitelist login POST endpoint
                    .anyRequest().authenticated()  // All other endpoints require authentication
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Stateless session management
            )
            .authenticationProvider(authenticationProvider());  // Register custom authentication provider
    return http.build();
  }
}