package com.innobridge.ethpay.configuration;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.innobridge.ethpay.service.CustomAuthenticationProvider;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
                                          CustomAuthenticationProvider authenticationProvider) throws Exception {
//      http.httpBasic(withDefaults());
//      http.authenticationProvider(authenticationProvider);
      http.authorizeHttpRequests((requests)
          -> requests.anyRequest().permitAll());
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

}
