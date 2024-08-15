package com.innobridge.ethpay.security;

import com.innobridge.ethpay.model.ExpirationTime;
import com.innobridge.ethpay.model.TokenType;
import com.innobridge.ethpay.model.User;
import com.innobridge.ethpay.model.UsernameEmailPasswordAuthenticationToken;
import com.innobridge.ethpay.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

import static com.innobridge.ethpay.model.TokenType.ACCESS_TOKEN;
import static com.innobridge.ethpay.model.TokenType.REFRESH_TOKEN;

@Component
public class JwtUtils {
    public static final ExpirationTime ACCESS_TOKEN_EXPIRATION_TIME = new ExpirationTime(0, 1, 1, 0);
    public static final ExpirationTime REFRESH_TOKEN_EXPIRATION_TIME = new ExpirationTime(0, 5, 5, 0);
    public static final String TOKEN_TYPE = "token_type";
    private final SecretKey accessSigningKey;
    private final SecretKey refreshSigningKey;

    private final UserService userService;

    @Autowired
    public JwtUtils(String accessSecretKey, String refreshSecretKey, UserService userService) {
        this.accessSigningKey = Keys.hmacShaKeyFor(accessSecretKey.getBytes());
        this.refreshSigningKey = Keys.hmacShaKeyFor(refreshSecretKey.getBytes());
        this.userService = userService;
    }

    /**
     * Generates a JWT token
     *
     * @param authentication UsernameEmailPasswordAuthenticationToken
     * @param tokenType      TokenType
     * @return JWT token
     */
    public String generateToken(UsernameEmailPasswordAuthenticationToken authentication, TokenType tokenType) {
        return generateToken(
                authentication.getId(),
                authentication.getName(),
                tokenType
        );
    }

    /**
     * Generates a JWT token
     *
     * @param id       User ID
     * @param userName User name
     * @param tokenType TokenType
     * @return JWT token
     */
   public String generateToken(String id, String userName, TokenType tokenType) {
        JwtBuilder jwts = Jwts.builder()
                .id(id)
                .subject(userName)
                .issuedAt(new Date(System.currentTimeMillis()));

        /**
         * If the token type is ACCESS_TOKEN, then set the expiration time to ACCESS_TOKEN_EXPIRATION_TIME and sign with the accessSigningKey
         * Otherwise, set the expiration time to REFRESH_TOKEN_EXPIRATION_TIME and sign with the refreshSigningKey
         */
        if (tokenType.equals(ACCESS_TOKEN)) {
            jwts
                    .claims(Map.of(TOKEN_TYPE, ACCESS_TOKEN))
                    .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME.toMillis()))
                    .signWith(accessSigningKey);
        } else {
            jwts
                    .claims(Map.of(TOKEN_TYPE, REFRESH_TOKEN))
                    .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME.toMillis()))
                    .signWith(refreshSigningKey);
        }
        return jwts.compact();
    }

    /**
     * Validates the JWT token
     *
     * @param token    JWT token
     * @param tokenType TokenType
     * @return UsernameEmailPasswordAuthenticationToken
     */
    public UsernameEmailPasswordAuthenticationToken validateToken(String token, TokenType tokenType) {

        /**
         * If the token type is ACCESS_TOKEN, then verify the token with the accessSigningKey
         * Otherwise, verify the token with the refreshSigningKey because we sign the different token types with different
         * signing keys.
         */
        Claims jwtPayload = Jwts.parser()
                .verifyWith(
                        tokenType.equals(ACCESS_TOKEN) ? accessSigningKey : refreshSigningKey
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();
        /**
         * The `parseSignedClaims` method is where we validate the jwt token. It checks
         * - if encrypt(base64(header) + "." + base64(payload), secretKey) == signature
         * - if the token is expired else throw JwtException
         */

        /**
         * Since we have 2 types of tokens(access, refresh), we want to check if the token type is valid.
         * The refresh token is used for the /auth/refresh and /auth/logout endpoint,
         * and the access token is used for all other endpoints. If validateToken is called through the /auth/refresh or
         * /auth/logout endpoint the parameter TokenType will be REFRESH_TOKEN, and the token type in the payload will be REFRESH_TOKEN
         * otherwise, the parameter TokenType will be ACCESS_TOKEN, and the token type in the payload will be ACCESS_TOKEN.
         */
        if (!jwtPayload.get(TOKEN_TYPE).equals(tokenType.name())) {
            throw new IllegalArgumentException("Invalid JWT token type");
        }

        // Retrieves the user from the database
        User user = userService.getById(jwtPayload.getId()).orElseThrow(
                () ->
                        new IllegalArgumentException("User not found"));

        /**
         * If stored in database does not equal the token from request, then throw an exception.
         * Because
         * - During logout we flush both the access and refresh token from database, or when we generate a new access token
         *  during refresh
         * - The token will still be valid if
         * we use parseSignedClaims to check if the encrypted header + payload == signature, or if the token is expired.
         * - But in terms of our business logic it is invalid, so we need to check if the token from the request is equal
         * to the token stored in the database
         */
        String StoredToken = tokenType.equals(ACCESS_TOKEN) ? user.getAccessToken() : user.getRefreshToken();
        if (StoredToken == null || !StoredToken.equals(token)) {
            throw new JwtException("JWT token does not exist");
        }

        /**
         * return verified Authentication object
         */
        return new UsernameEmailPasswordAuthenticationToken(jwtPayload.getId(), jwtPayload.getSubject(), user.getAuthorities());
    }

//    public String generateTokenFromRefreshToken(String refreshToken) {
//        // todo
//        return null;
////        return generateToken(Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(refreshToken).getBody().getSubject());
//    }
}