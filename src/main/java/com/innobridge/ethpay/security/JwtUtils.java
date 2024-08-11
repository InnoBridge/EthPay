package com.innobridge.ethpay.security;

import com.innobridge.ethpay.model.ExpirationTime;
import com.innobridge.ethpay.model.TokenType;
import com.innobridge.ethpay.model.UsernameEmailPasswordAuthenticationToken;
import com.innobridge.ethpay.service.UserService;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

import static com.innobridge.ethpay.model.TokenType.ACCESS_TOKEN;
import static com.innobridge.ethpay.model.TokenType.REFRESH_TOKEN;

@Component
public class JwtUtils {
    public static final ExpirationTime ACCESS_TOKEN_EXPIRATION_TIME = new ExpirationTime(0, 0, 1, 0);
    public static final ExpirationTime REFRESH_TOKEN_EXPIRATION_TIME = new ExpirationTime(0, 0, 5, 0);
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

    public String generateToken(UsernameEmailPasswordAuthenticationToken authentication, TokenType tokenType) {

        return generateToken(
                authentication.getId(),
                authentication.getName(),
                tokenType
        );
    }
    String generateToken(String id, String userName, TokenType tokenType) {
        JwtBuilder jwts = Jwts.builder()
                .id(id)
                .subject(userName)
                .issuedAt(new Date(System.currentTimeMillis()));

        if (tokenType == ACCESS_TOKEN) {
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

//    public UsernameEmailPasswordAuthenticationToken validateToken(String token) {
//        return validateToken(token, signingKey);
//    }
//
//    public UsernameEmailPasswordAuthenticationToken validateToken(String token, SecretKey key) {
//
//            Claims jwtPayload = Jwts.parser()
//                    .verifyWith(key)
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//
//            User user = userService.getById(jwtPayload.getId()).orElseThrow(
//                    () ->
//                    new AuthenticationServiceException("Invalid JWT access token"));
//
//            System.out.println("Token Type: " + jwtPayload.get(TOKEN_TYPE));
//
//        if (jwtPayload != null) {
//            return new UsernameEmailPasswordAuthenticationToken(jwtPayload.getId(), jwtPayload.getSubject(), new ArrayList<>());
//        }
//        return null;
//    }

//    public String generateTokenFromRefreshToken(String refreshToken) {
//        // todo
//        return null;
////        return generateToken(Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(refreshToken).getBody().getSubject());
//    }

//    private Map<String, Object> getClaimsFromAuthentication(UsernameEmailPasswordAuthenticationToken authentication){
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("authorities", authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .toList());
//        return claims;
//    }
}