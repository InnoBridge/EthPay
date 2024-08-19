package com.innobridge.ethpay.security;

import com.innobridge.ethpay.model.User;
import com.innobridge.ethpay.model.UsernameEmailPasswordAuthenticationToken;
import com.innobridge.ethpay.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static com.innobridge.ethpay.constants.HTTPConstants.CONTENT_TYPE;
import static com.innobridge.ethpay.constants.HTTPConstants.REFRESH_COOKIE;
import static com.innobridge.ethpay.model.TokenType.ACCESS_TOKEN;
import static com.innobridge.ethpay.model.TokenType.REFRESH_TOKEN;
import static com.innobridge.ethpay.security.JwtUtils.REFRESH_TOKEN_EXPIRATION_TIME;

public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private String redirectUri = "http://localhost:8080/oauth2/success";
    @Autowired
    UserService userService;
    @Autowired
    JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        handle(request, response, authentication);
        super.clearAuthenticationAttributes(request);
    }

    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication oauthAuthentication) throws IOException {
        String targetUrl = redirectUri.isEmpty() ?
                determineTargetUrl(request, response, oauthAuthentication) : redirectUri;
        OAuth2AuthenticationToken oauth2Authentication = (OAuth2AuthenticationToken) oauthAuthentication;
        if (!Boolean.TRUE.equals(oauth2Authentication.getPrincipal().getAttribute("email_verified"))) {
            throw new OAuth2AuthenticationException("Email not verified");
        }
        String email = oauth2Authentication.getPrincipal().getAttribute("email");

        User user = userService.getByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found with email: " + email)
        );
        UsernameEmailPasswordAuthenticationToken authentication = new UsernameEmailPasswordAuthenticationToken(user.getId(), user.getUsername(), user.getAuthorities());

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

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("accessToken", accessToken)
                .build().toUriString();
        response.setContentType(CONTENT_TYPE);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
