package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.*;
import com.innobridge.ethpay.security.JwtUtils;
import com.innobridge.ethpay.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;

import static com.innobridge.ethpay.model.TokenType.ACCESS_TOKEN;
import static com.innobridge.ethpay.model.TokenType.REFRESH_TOKEN;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signup")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful signup",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SignupResponse.class)))
    })
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        if (userService.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (userService.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(signupRequest.getPassword());
        user.setAuthorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);

        User savedUser = userService.saveUser(user);

        // Return success response with email, username, and userid
        return ResponseEntity.ok(new SignupResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail()));
    }

    @PostMapping("/signin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful signin",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SigninResponse.class)))
    })
    public ResponseEntity<?> authenticateUser(@RequestBody SigninRequest signinRequest) {
        String username = signinRequest.getUsername();
        String email = signinRequest.getEmail();
        String password = signinRequest.getPassword();

        // Check if both username and email are missing
        if ((username == null || username.isEmpty()) && (email == null || email.isEmpty())) {
            throw new AuthenticationServiceException("Authentication failed: need both username or email.");
        }

        // Check if password is missing
        if (password == null || password.isEmpty()) {
            throw new AuthenticationServiceException("Authentication failed: need password.");
        }

        // Determine whether to authenticate by username or email
        boolean withUsername = (username != null && !username.isEmpty());

        // Use username if available, otherwise email
        String principal = withUsername ? username : email;


        try {
            // Create custom authentication token
            UsernameEmailPasswordAuthenticationToken authRequest = new UsernameEmailPasswordAuthenticationToken(principal, password, withUsername);

            UsernameEmailPasswordAuthenticationToken authentication = (UsernameEmailPasswordAuthenticationToken) authenticationManager.authenticate(authRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);


            // Generate JWT token
            String accessToken = jwtUtils.generateToken(authentication, ACCESS_TOKEN);
            String refreshToken = jwtUtils.generateToken(authentication, REFRESH_TOKEN);

            userService.updateTokens(authentication.getId(), accessToken, refreshToken);

            return ResponseEntity.ok(new SigninResponse(accessToken, refreshToken));
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Error: " + e.getMessage());
        }
    }

}