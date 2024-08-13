package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.*;
import com.innobridge.ethpay.security.JwtUtils;
import com.innobridge.ethpay.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

import static com.innobridge.ethpay.constants.HTTPConstants.*;
import static com.innobridge.ethpay.model.TokenType.ACCESS_TOKEN;
import static com.innobridge.ethpay.model.TokenType.REFRESH_TOKEN;
import static com.innobridge.ethpay.security.JwtUtils.ACCESS_TOKEN_EXPIRATION_TIME;
import static com.innobridge.ethpay.security.JwtUtils.REFRESH_TOKEN_EXPIRATION_TIME;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RestController
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping(SIGNUP_URL)
    @ApiResponses(value = {
            @ApiResponse(responseCode = CREATED,
                    description = "Successful signup",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = SignupResponse.class)))
    })
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest, HttpServletResponse response) {
        if (userService.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .status(BAD_REQUEST)
                    .contentType(APPLICATION_JSON)
                    .body("Error: Username is already taken!");

        }

        if (userService.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .status(BAD_REQUEST)
                    .contentType(APPLICATION_JSON)
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
        return ResponseEntity.status(HttpStatus.CREATED).body(new SignupResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail()));
    }

    @PostMapping(SIGNIN_URL)
    @ApiResponses(value = {
            @ApiResponse(responseCode = CREATED, description = "Successful signin",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = AccessTokenResponse.class)))
    })
    public ResponseEntity<?> authenticateUser(@RequestBody SigninRequest signinRequest, HttpServletResponse response) {

        UsernameEmailPasswordAuthenticationToken authentication = (UsernameEmailPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        String accessToken = jwtUtils.generateToken(authentication, ACCESS_TOKEN);
        String refreshToken = jwtUtils.generateToken(authentication, REFRESH_TOKEN);

        userService.updateTokens(authentication.getId(), accessToken, refreshToken);

        // Set refresh token in HTTP-only cookie
        Cookie refreshTokenCookie = new Cookie(REFRESH_COOKIE, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // should be set to true in production
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) REFRESH_TOKEN_EXPIRATION_TIME.toSeconds()); // expires in 7 days
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(new AccessTokenResponse(accessToken, ACCESS_TOKEN_EXPIRATION_TIME.toSeconds()));
    }

    @PostMapping(REFRESH_TOKEN_URL)
    @ApiResponses(value = {
            @ApiResponse(responseCode = CREATED, description = "Refresh token successful",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = AccessTokenResponse.class)))
    })
    public ResponseEntity<?> refreshToken(HttpServletResponse response) {
        UsernameEmailPasswordAuthenticationToken authentication = (UsernameEmailPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        String accessToken = jwtUtils.generateToken(authentication, ACCESS_TOKEN);
        userService.updateAccessToken(authentication.getId(), accessToken);

        return ResponseEntity.ok(new AccessTokenResponse(accessToken, ACCESS_TOKEN_EXPIRATION_TIME.toSeconds()));
    }

    @PostMapping(SIGNOUT_URL)
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie(REFRESH_COOKIE, null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Set to true if using HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // Expire the cookie
        response.addCookie(refreshTokenCookie);

        // Get the authenticated user's ID from security context
        String userId = ((UsernameEmailPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getId();
        userService.deleteTokens(userId);

        return ResponseEntity.ok("Signout successful");
    }
}