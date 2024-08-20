package com.innobridge.ethpay.security;

import com.innobridge.ethpay.model.User;
import com.innobridge.ethpay.model.UsernameEmailPasswordAuthenticationToken;
import com.innobridge.ethpay.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

import static com.innobridge.ethpay.constants.HTTPConstants.*;
import static com.innobridge.ethpay.model.TokenType.ACCESS_TOKEN;
import static com.innobridge.ethpay.model.TokenType.REFRESH_TOKEN;
import static com.innobridge.ethpay.security.JwtUtils.REFRESH_TOKEN_EXPIRATION_TIME;

public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${OAUTH2_REDIRECT_BASE_URI}")
    private String baseRedirectUri;
    private String redirectUri;
    private String failureRedirectUri;
    @Autowired
    UserService userService;
    @Autowired
    JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        this.redirectUri = baseRedirectUri + OAUTH2_SUCCESS_URL;
        this.failureRedirectUri = baseRedirectUri + OAUTH2_FAILURE_URL;
        handle(request, response, authentication);
        super.clearAuthenticationAttributes(request);
    }

    /**
     * We are close to end of the OAuth2 flow, after the Client redirected the user to the OAuth2 provider,
     * after the user sends its credential/consent to the OAuth2 provider, the OAuth2 provider redirects the user back to the Client.
     * providing the authorization code/token to the Client to retrieve the users' information.
     * This method handles the successfull retrieval of the user's information from the OAuth2 provider.
     * Where we validate that the user's email matches the email in the database, and using the user's information
     * we generate the JWT access/refresh token, in which we redirect to the /oauth2/success endpoint otherwise
     * we redirect to the /oauth2/failure endpoint.
     */
    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication oauthAuthentication) throws IOException {
        String targetUrl = redirectUri.isEmpty() ?
                determineTargetUrl(request, response, oauthAuthentication) : redirectUri;
        OAuth2AuthenticationToken oauth2Authentication = (OAuth2AuthenticationToken) oauthAuthentication;
        try {
            if (!Boolean.TRUE.equals(oauth2Authentication.getPrincipal().getAttribute("email_verified"))) {
                targetUrl = UriComponentsBuilder.fromUriString(failureRedirectUri)
                        .queryParam("error", "Email not verified")
                        .build().toUriString();
                response.setContentType(CONTENT_TYPE);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }
            String email = oauth2Authentication.getPrincipal().getAttribute("email");

            Optional<User> optionalUser = userService.getByEmail(email);
            if(optionalUser.isEmpty()) {
                String failureUrl = UriComponentsBuilder.fromUriString(failureRedirectUri)
                        .queryParam("error", "User not found with email: " + email)
                        .build().toUriString();
                response.setContentType(CONTENT_TYPE);
                getRedirectStrategy().sendRedirect(request, response, failureUrl);
                return;
            }

            User user = optionalUser.get();

            UsernameEmailPasswordAuthenticationToken authentication = new UsernameEmailPasswordAuthenticationToken(user.getId(), user.getUsername(), user.getAuthorities());

            String accessToken = jwtUtils.generateToken(authentication, ACCESS_TOKEN);
            String refreshToken = jwtUtils.generateToken(authentication, REFRESH_TOKEN);

            userService.updateTokens(authentication.getId(), accessToken, refreshToken);

            // Set refresh token in HTTP-only cookie
            Cookie refreshTokenCookie = getRefreshTokenCookie(refreshToken);
            response.addCookie(refreshTokenCookie);

            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("accessToken", accessToken)
                    .build().toUriString();

            response.setContentType(CONTENT_TYPE);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (AuthenticationException e) {
            targetUrl = UriComponentsBuilder.fromUriString(failureRedirectUri)
                    .queryParam("error", e.getMessage())
                    .build().toUriString();
            response.setContentType(CONTENT_TYPE);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

    private Cookie getRefreshTokenCookie(String refreshToken) {
        Cookie refreshTokenCookie = new Cookie(REFRESH_COOKIE, refreshToken);
        refreshTokenCookie.setHttpOnly(true); // prevents JavaScript from accessing the cookie
        refreshTokenCookie.setSecure(true); // should be set to true in production
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) REFRESH_TOKEN_EXPIRATION_TIME.toSeconds()); // Evicts the cookie from browser when the token expires
        return refreshTokenCookie;
    }
}
