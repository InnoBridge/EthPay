package com.innobridge.ethpay.configuration;

import com.innobridge.ethpay.security.*;
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
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.innobridge.ethpay.constants.HTTPConstants.*;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
  public ClientRegistrationRepository clientRegistrationRepository(
          @Value("${GOOGLE_CLIENT_ID}") String googleClientId,
          @Value("${GOOGLE_CLIENT_SECRET}") String googleClientSecret,
          @Value("${OAUTH2_REDIRECT_BASE_URI}") String baseRedirectUri) {
    ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(GOOGLE_ID)
            .clientId(googleClientId)
            .clientSecret(googleClientSecret)
            .clientAuthenticationMethod(CLIENT_SECRET_BASIC)
            .authorizationGrantType(AUTHORIZATION_CODE)
            .redirectUri(baseRedirectUri + GOOGLE_REDIRECT_URI_TEMPLATE)
            .scope(GOOGLE_SCOPES)
            .authorizationUri(GOOGLE_AUTHORIZATION_URI)
            .tokenUri(GOOGLE_TOKEN_URI)
            .userInfoUri(GOOGLE_USER_INFO_URI)
            .jwkSetUri(GOOGLE_JWK_SET_URI)
            .userNameAttributeName(OAUTH2_USER_NAME_ATTRIBUTE)
            .clientName(GOOGLE_CLIENT_NAME)
            .build();
    return new InMemoryClientRegistrationRepository(clientRegistration);
  }

  @Bean
  public CustomOAuth2SuccessHandler customAuthenticationSuccessHandler() {
    return new CustomOAuth2SuccessHandler();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 JwtAuthenticationFilter jwtAuthenticationFilter,
                                                 UsernameEmailPasswordAuthenticationFilter usernameEmailPasswordAuthenticationFilter,
                                                 ClientRegistrationRepository clientRegistrationRepository,
                                                 CustomOAuth2SuccessHandler customAuthenticationSuccessHandler) throws Exception {
    http
            .csrf(csrf -> csrf.disable())  // Disable CSRF protection
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(WHITE_LIST_URL).permitAll()  // Whitelist signup endpoint
                    .requestMatchers(SIGNIN_METHOD, SIGNIN_URL).permitAll() // Whitelist login POST endpoint
                    .anyRequest().authenticated()  // All other endpoints require authentication
            )
            /**
             * Because we are using JWT(self contain) tokens, we can make the api calls stateless, which means
             * we don't need to store the session in the server. But for OAuth2 flows, because we need to make multiple
             * calls between the client(our application), user, and the google authorization server, to exchange user
             * credentials for oauth2 tokens, and exchange the tokens for user information to authenticate the user.
             * We need to use sessions stored on the client to keep track of the keys and personal information that
             * is propagated during the sequence of api calls.
             * So for the sequence of api calls for OAuth2 authentication we need the REST calls to be stateful, while
             * for the rest of the api calls we can make them stateless.
             */
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Default to stateless
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Stateful for OAuth2 flows
                    .sessionFixation().none()  // No session fixation protection
            )
            .oauth2Login(oauth2 ->
                    oauth2.clientRegistrationRepository(clientRegistrationRepository)// Ensure OAuth2 login is configured
                            .successHandler(customAuthenticationSuccessHandler))
            .authenticationProvider(authenticationProvider())  // Register custom authentication provider
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(usernameEmailPasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}