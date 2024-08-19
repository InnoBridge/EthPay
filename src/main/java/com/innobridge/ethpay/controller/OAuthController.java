package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.AccessTokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.innobridge.ethpay.security.JwtUtils.ACCESS_TOKEN_EXPIRATION_TIME;

@RestController
public class OAuthController {

    @GetMapping("/oauth2/success")
    public ResponseEntity<?> success(@RequestParam String accessToken, HttpServletResponse response) {

        return ResponseEntity.ok(new AccessTokenResponse(accessToken, ACCESS_TOKEN_EXPIRATION_TIME.toSeconds()));
    }
}