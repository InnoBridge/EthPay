package com.innobridge.ethpay.configuration;

import com.innobridge.ethpay.security.*;
import com.innobridge.ethpay.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.innobridge.ethpay.constants.HTTPConstants.*;
import static com.innobridge.ethpay.constants.HTTPConstants.OAUTH2_URL;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC;

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
//          "/oauth2/authorization/google",
//          "/login/oauth2/code/google",
          "/oauth2/**"
  };

  // Define your matcher to identify OAuth2-related requests
  private static final RequestMatcher OAUTH2_REQUEST_MATCHER = new RequestMatcher() {
    private final String OAUTH2_BASE_URI = "/oauth2/";

    @Override
    public boolean matches(HttpServletRequest request) {
      // Check if the request URI is related to OAuth2 flow
      return request.getRequestURI().contains(OAUTH2_BASE_URI);
    }
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
  public ClientRegistrationRepository clientRegistrationRepository(
          @Value("${GOOGLE_CLIENT_ID}") String googleClientId,
          @Value("${GOOGLE_CLIENT_SECRET}") String googleClientSecret,
          @Value("${OAUTH2_REDIRECT_BASE_URI}") String baseRedirectUri) {
    ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
            .clientId(googleClientId)
            .clientSecret(googleClientSecret)
            .clientAuthenticationMethod(CLIENT_SECRET_BASIC)
            .authorizationGrantType(AUTHORIZATION_CODE)
            .redirectUri(baseRedirectUri + "/login/oauth2/code/google")
            .scope("openid", "profile", "email")
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .userNameAttributeName("sub")
            .clientName("Google")
            .build();

    return new InMemoryClientRegistrationRepository(clientRegistration);

  }

  @Bean
  public CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler() {
    return new CustomAuthenticationSuccessHandler();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 JwtAuthenticationFilter jwtAuthenticationFilter,
                                                 UsernameEmailPasswordAuthenticationFilter usernameEmailPasswordAuthenticationFilter,
                                                 ClientRegistrationRepository clientRegistrationRepository,
                                                 CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) throws Exception {
    http
            .csrf(csrf -> csrf.disable())  // Disable CSRF protection
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(WHITE_LIST_URL).permitAll()  // Whitelist signup endpoint
                    .requestMatchers(SIGNIN_METHOD, SIGNIN_URL).permitAll() // Whitelist login POST endpoint
                    .anyRequest().authenticated()  // All other endpoints require authentication
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Default to stateless
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Stateful for OAuth2 flows
                    .sessionFixation().none()  // No session fixation protection
            )
            .oauth2Login(oauth2 ->
                    oauth2.clientRegistrationRepository(clientRegistrationRepository)// Ensure OAuth2 login is configured
                            .successHandler(customAuthenticationSuccessHandler)
//                            .failureUrl("/oauth2/failure")
            )
            .authenticationProvider(authenticationProvider())  // Register custom authentication provider
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(usernameEmailPasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}