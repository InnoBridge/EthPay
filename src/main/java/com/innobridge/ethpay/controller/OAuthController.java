package com.innobridge.ethpay.controller;

import com.innobridge.ethpay.model.AccessTokenResponse;
import com.innobridge.ethpay.model.Exchange;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.innobridge.ethpay.constants.HTTPConstants.*;
import static com.innobridge.ethpay.constants.HTTPConstants.CONTENT_TYPE;
import static com.innobridge.ethpay.security.JwtUtils.ACCESS_TOKEN_EXPIRATION_TIME;

@RestController
public class OAuthController {

    @GetMapping(OAUTH2_SUCCESS_URL)
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "After the user's information is successfully retrieved from the OAuth2 provider, " +
                    "the user is validated and the access/refresh token is generated is redirected to this endpoint to present to the user.",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = AccessToken.class)))
    })
    public ResponseEntity<?> success(@RequestParam String accessToken, HttpServletResponse response) {
        /**
         * Since the api calls is stateful during the OAuth2 flow, and this endpoint marks the end of the OAuth2 flow,
         * we remove the JSESSIONID cookie to revert back to stateless api calls.
         */
        response.addCookie(removeJSessionIdCookie());
        try {
            return ResponseEntity.ok(new AccessTokenResponse(accessToken, ACCESS_TOKEN_EXPIRATION_TIME.toSeconds()));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping(OAUTH2_FAILURE_URL)
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "Failure page when OAuth2 Authentication fails.",
                    content = @Content(mediaType = CONTENT_TYPE,
                            schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> failure(@RequestParam String error, HttpServletResponse response) {
        /**
         * Since the api calls is stateful during the OAuth2 flow, and this endpoint marks the end of the OAuth2 flow,
         * we remove the JSESSIONID cookie to revert back to stateless api calls.
         */
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