package com.innobridge.ethpay.security;

import com.innobridge.ethpay.model.ExpirationTime;
import com.innobridge.ethpay.model.TokenType;
import com.innobridge.ethpay.model.User;
import com.innobridge.ethpay.model.UsernameEmailPasswordAuthenticationToken;
import com.innobridge.ethpay.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
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
         * Otherwise, verify the token with the refreshSigningKey
         */
        Claims jwtPayload = Jwts.parser()
                .verifyWith(
                        tokenType.equals(ACCESS_TOKEN) ? accessSigningKey : refreshSigningKey
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();

        /**
         * If the token type is not the same as the token type in the JWT payload, then throw an exception
         */
        if (!jwtPayload.get(TOKEN_TYPE).equals(tokenType.name())) {
            throw new IllegalArgumentException("Invalid JWT token type");
        }

        User user = userService.getById(jwtPayload.getId()).orElseThrow(
                () ->
                        new IllegalArgumentException("User not found"));

        /**
         * If stored in database does not equal the token from request, then throw an exception
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