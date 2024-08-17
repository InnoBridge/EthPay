package com.innobridge.ethpay.configuration;

import com.innobridge.ethpay.security.JwtAuthenticationFilter;
import com.innobridge.ethpay.security.JwtUtils;
import com.innobridge.ethpay.security.UsernameEmailPasswordAuthenticationFilter;
import com.innobridge.ethpay.security.UsernameEmailPasswordAuthenticationProvider;
import com.innobridge.ethpay.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.innobridge.ethpay.constants.HTTPConstants.*;
import static com.innobridge.ethpay.constants.HTTPConstants.OAUTH2_URL;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  public static final String[] WHITE_LIST_URL = {
          PUBLIC_URL,
          SWAGGER_UI_URL,
          SWAGGER_RESOURCES_URL,
          SWAGGER_RESOURCES_ALL_URL,
          API_DOCS_URL,
          API_DOCS_ALL_URL,
          SIGNUP_URL,
          OAUTH2_URL,
          CREATE_PAYMENT_URL,
          UPDATE_PAYMENT_URL
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
  public UsernameEmailPasswordAuthenticationFilter usernameEmailPasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
    return new UsernameEmailPasswordAuthenticationFilter(authenticationManager);
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtils jwtUtils) {
    return new JwtAuthenticationFilter(jwtUtils);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 JwtAuthenticationFilter jwtAuthenticationFilter,
                                                 UsernameEmailPasswordAuthenticationFilter usernameEmailPasswordAuthenticationFilter) throws Exception {
    http
            .csrf(csrf -> csrf.disable())  // Disable CSRF protection
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(WHITE_LIST_URL).permitAll()  // Whitelist signup endpoint
                    .requestMatchers(SIGNIN_METHOD, SIGNIN_URL).permitAll() // Whitelist login POST endpoint
                    .anyRequest().authenticated()  // All other endpoints require authentication
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Stateless session management
            )
            .authenticationProvider(authenticationProvider())  // Register custom authentication provider
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(usernameEmailPasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}