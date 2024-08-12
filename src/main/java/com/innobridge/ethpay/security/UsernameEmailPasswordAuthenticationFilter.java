package com.innobridge.ethpay.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innobridge.ethpay.controller.AuthenticationController;
import com.innobridge.ethpay.model.SigninRequest;
import com.innobridge.ethpay.model.UsernameEmailPasswordAuthenticationToken;
import com.innobridge.ethpay.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UsernameEmailPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private AuthenticationController authenticationController;
    public UsernameEmailPasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        setFilterProcessesUrl("/auth/signin");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        SigninRequest signinRequest = extractSigninRequest(request);

        String username = signinRequest.getUsername();
        String email = signinRequest.getEmail();
        String password = signinRequest.getPassword();

        if ((username == null || username.isEmpty()) && (email == null || email.isEmpty())) {
            throw new AuthenticationServiceException("Authentication failed: need both username or email.");
        }

        if (password == null || password.isEmpty()) {
            throw new AuthenticationServiceException("Authentication failed: need password.");
        }

        boolean withUsername = (username != null && !username.isEmpty());
        String principal = withUsername ? username : email;

        UsernameEmailPasswordAuthenticationToken authRequest = new UsernameEmailPasswordAuthenticationToken(principal, password, withUsername);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);

        ResponseEntity<?> responseEntity = authenticationController.authenticateUser(null);

        response.setContentType("application/json");
        response.setStatus(responseEntity.getStatusCode().value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseEntity.getBody()));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        ResponseEntity<String> responseEntity = ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Error: " + failed.getMessage());

        response.setContentType("application/json");
        response.setStatus(responseEntity.getStatusCode().value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseEntity.getBody()));
    }

    private SigninRequest extractSigninRequest(HttpServletRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(request.getInputStream(), SigninRequest.class);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Authentication failed: unable to read request body.", e);
        }
    }
}
