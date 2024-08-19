package com.innobridge.ethpay.constants;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class HTTPConstants {
    // Url endpoints
    public static final String PUBLIC_URL = "/public/**";
    public static final String SWAGGER_UI_URL = "/swagger-ui/**";
    public static final String SWAGGER_RESOURCES_URL = "/swagger-resources/";
    public static final String SWAGGER_RESOURCES_ALL_URL = "/swagger-resources/**";
    public static final String API_DOCS_URL = "/v3/api-docs";
    public static final String API_DOCS_ALL_URL = "/v3/api-docs/**";
    public static final String SIGNIN_URL = "/auth/signin";
    public static final String SIGNUP_URL = "/auth/signup";
    public static final String SIGNOUT_URL = "/auth/signout";
    public static final HttpMethod SIGNIN_METHOD = HttpMethod.POST;

    public static final String REFRESH_TOKEN_URL = "/auth/refresh";
    public static final String CONTACTS_URL = "/contacts";

    public static final String EXCHANGE_URL = "/exchange";
    public static final String ACCOUNT_URL = "/account";
    public static final String TRANSACTION_URL = "/transaction";
    public static final String PROFILE_URL = "/profile";

    public static final String OAUTH2_URLS = "/oauth2/**";
    public static final String OAUTH2_BASE_URI = "/oauth2/";
    public static final String[] WHITE_LIST_URL = {
            PUBLIC_URL,
            SWAGGER_UI_URL,
            SWAGGER_RESOURCES_URL,
            SWAGGER_RESOURCES_ALL_URL,
            API_DOCS_URL,
            API_DOCS_ALL_URL,
            SIGNUP_URL,
            OAUTH2_URLS
    };

    // Define your matcher to identify OAuth2-related requests
    private static final RequestMatcher OAUTH2_REQUEST_MATCHER = new RequestMatcher() {
        @Override
        public boolean matches(HttpServletRequest request) {
            // Check if the request URI is related to OAuth2 flow
            return request.getRequestURI().contains(OAUTH2_BASE_URI);
        }
    };

    public static final String ACCESS_TOKEN = "access-token";

    public static final String ACCESS_COOKIE = "access-token";
    public static final String REFRESH_COOKIE = "refresh-token";

    public static final String OK = "200";
    public static final String CREATED = "201";

    public static final String CONTENT_TYPE = "application/json";

}
