package com.innobridge.ethpay.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innobridge.ethpay.model.TokenType;
import com.innobridge.ethpay.model.UsernameEmailPasswordAuthenticationToken;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static com.innobridge.ethpay.constants.HTTPConstants.*;
import static com.innobridge.ethpay.model.TokenType.ACCESS_TOKEN;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer";
    private static final Set<String> REFRESH_COOKIE_URL = Set.of(SIGNOUT_URL, REFRESH_TOKEN_URL);


    private final JwtUtils jwtUtils;
    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
        * Filters the request and validates the JWT token
        *
        * @param request HTTP request
        * @param response HTTP response
        * @param filterChain Filter chain
        * @throws ServletException
        * @throws IOException
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            /**
             * If the request url is whitelisted, then no need to validate the JWT token
             */
            if (isWhitelistedOrAuthenticated(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            boolean isRefreshCookieUrl = REFRESH_COOKIE_URL.contains(request.getRequestURI());
            TokenType tokenType = isRefreshCookieUrl ? TokenType.REFRESH_TOKEN : ACCESS_TOKEN;

            String token = getJwtFromRequest(request, isRefreshCookieUrl);

            UsernameEmailPasswordAuthenticationToken authentication = jwtUtils.validateToken(token, tokenType);

            /**
             * Store the authenticated authentication object in the security context
             * so userId, authorities can be used by downstream components, e.g. controllers, services, etc.
             */
            if (getContext().getAuthentication() == null) {
                getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (AuthenticationException | JwtException failed) {
            handleUnsuccessfulAuthentication(request, response, failed);
        }
    }

    /**
        * Handles unsuccessful authentication
        *
        * @param request HTTP request
        * @param response HTTP response
        * @param failed Authentication exception
        * @throws IOException
     */
    private void handleUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, RuntimeException failed) throws IOException {
        ResponseEntity<String> responseEntity = ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Error: " + failed.getMessage());

        response.setContentType(CONTENT_TYPE);
        response.setStatus(responseEntity.getStatusCode().value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseEntity.getBody()));
    }

        /**
            * Extracts JWT token from the request
            *
            * @param request HTTP request
            * @return JWT token
         */
    private String getJwtFromRequest(HttpServletRequest request, boolean isRefreshCookieUrl) {
        Cookie[] cookies = request.getCookies();

        /**
         * If the request URL is a refresh token URL, then extract the refresh-token from the cookie
         */
        if (isRefreshCookieUrl) {
            if (cookies != null) {
                return Arrays.stream(cookies)
                        .filter(cookie -> REFRESH_COOKIE.equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElseThrow(() -> new JwtException("Refresh token is missing"));
            }
        }

        /**
         * For all other url extract the access token from the Authorization header or the access-token cookie
         */
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith(BEARER + " ")) {
            return bearerToken.substring(7);
        }

        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> ACCESS_COOKIE.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElseThrow(() -> new JwtException("Access token is missing"));
        }

        throw new JwtException("JWT token is missing");
    }

    /**
        * Checks if the request URL is whitelisted
        *
        * @param request HTTP request
        * @return true if the request URL is whitelisted, false otherwise
     */
    private boolean isWhitelistedOrAuthenticated(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        Authentication authentication = getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return true;
        }

        AntPathMatcher pathMatcher = new AntPathMatcher();
        boolean isWhitelistedURL = Stream.of(WHITE_LIST_URL)
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
        return isWhitelistedURL;
    }
}