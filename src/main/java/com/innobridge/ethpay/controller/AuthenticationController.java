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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        /*
         * Username and email must be unique for each user, following checks if the provided email or
         * username are already taken.
         */
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

        Map<Currency, BigDecimal> balance = new HashMap<>();
        balance.put(Currency.USD, new BigDecimal(10000.00));
        balance.put(Currency.CAD, new BigDecimal(10000.00));
        balance.put(Currency.JPY, new BigDecimal(15000.00));
        balance.put(Currency.EUR, new BigDecimal(10000.00));

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(signupRequest.getPassword());
        user.setAuthorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user.setBalance(balance);

        User savedUser = userService.saveUser(user);

        // Return success response with email, username, and userid
        return ResponseEntity.status(HttpStatus.CREATED).body(new SignupResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail()));
    }

    /**
     * When the username/email and password are validated during signin, an access token and refresh token are returned
     * to the user. The access token is used to authenticate the user for a short period of time, so it is passed back to
     * user in the response body along with the expiry time.
     * While the refresh token are longer lived and are stored in an HTTP-only cookie on the user's browser.
     */
    @PostMapping(SIGNIN_URL)
    @ApiResponses(value = {
            @ApiResponse(responseCode = CREATED, description = "Successful signin",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = AccessTokenResponse.class)))
    })
    public ResponseEntity<?> authenticateUser(@RequestBody SigninRequest signinRequest, HttpServletResponse response) {
        /** The reason for storing the authentication object in the SecurityContextHolder because SecurityContextHolder is thread local
         * store meaning that our authentication object is global to the thread. You can access the authentication object from
         * anywhere in the application without worrying about leaking the authentication object to other threads. Because while
         * SecurityContextHolder is global to the thread, it provides isolation because the authentication object isolated to
         * the thread. By thread, we mean the request thread that is processing the request.
         * We stored the userId, username, email, and authorities in the authentication object, which means we can
         * access the user's information from anywhere in the application.
         */
        UsernameEmailPasswordAuthenticationToken authentication = (UsernameEmailPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        String accessToken = jwtUtils.generateToken(authentication, ACCESS_TOKEN);
        String refreshToken = jwtUtils.generateToken(authentication, REFRESH_TOKEN);

        userService.updateTokens(authentication.getId(), accessToken, refreshToken);

        // Set refresh token in HTTP-only cookie
        Cookie refreshTokenCookie = new Cookie(REFRESH_COOKIE, refreshToken);
        refreshTokenCookie.setHttpOnly(true); // prevents JavaScript from accessing the cookie
        refreshTokenCookie.setSecure(true); // should be set to true in production
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) REFRESH_TOKEN_EXPIRATION_TIME.toSeconds()); // Evicts the cookie from browser when the token expires
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(new AccessTokenResponse(accessToken, ACCESS_TOKEN_EXPIRATION_TIME.toSeconds()));
    }

    /**
     * Uses the refresh token to authenticate the user and generate a new access token. We use the refresh token for authentication
     * because the user would call this endpoint to get a new access token when the current access token expires.
     * A new access token is generated and returned to the user, and replace the old access token in the database with the new access token.
     */
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

    /**
     * We are using the refresh token to authenticate the user during logout.
     * When the user is successfully authenticated we remove the user's access and refresh token from the database.
     * We will also remove the refresh token from the user's browser by setting the cookie to expire immediately.
     */
    @PostMapping(SIGNOUT_URL)
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        // Get the authenticated user's ID from security context
        String userId = ((UsernameEmailPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getId();
        userService.deleteTokens(userId);

        Cookie refreshTokenCookie = new Cookie(REFRESH_COOKIE, null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // Remove the refresh token from the user's browser by setting the cookie to expire immediately.
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok("Signout successful");
    }
}