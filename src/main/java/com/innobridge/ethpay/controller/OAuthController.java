package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.AccessTokenResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.innobridge.ethpay.constants.HTTPConstants.OAUTH2_FAILURE_URL;
import static com.innobridge.ethpay.constants.HTTPConstants.OAUTH2_SUCCESS_URL;
import static com.innobridge.ethpay.security.JwtUtils.ACCESS_TOKEN_EXPIRATION_TIME;

@RestController
public class OAuthController {

    @GetMapping(OAUTH2_SUCCESS_URL)
    public ResponseEntity<?> success(@RequestParam String accessToken, HttpServletResponse response) {
        // Remove JSESSIONID cookie
        response.addCookie(removeJSessionIdCookie());
        try {
            return ResponseEntity.ok(new AccessTokenResponse(accessToken, ACCESS_TOKEN_EXPIRATION_TIME.toSeconds()));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping(OAUTH2_FAILURE_URL)
    public ResponseEntity<?> failure(@RequestParam String error, HttpServletResponse response) {
        // Remove JSESSIONID cookie
        response.addCookie(removeJSessionIdCookie());
        return ResponseEntity.badRequest().body(error);
    }

    private Cookie removeJSessionIdCookie() {
        Cookie jsessionidCookie = new Cookie("JSESSIONID", null);
        jsessionidCookie.setPath("/");
        jsessionidCookie.setMaxAge(0);
        return jsessionidCookie;
    }
}