package com.innobridge.ethpay.constants;

import org.springframework.http.HttpMethod;

public class UrlConstants {
    public static final String SIGNIN_URL = "/auth/signin";
    public static final String SIGNUP_URL = "/auth/signup";
    public static final String SIGNOUT_URL = "/auth/signout";
    public static final String PUBLIC_URL = "/public/**";
    public static final String SWAGGER_UI_URL = "/swagger-ui/**";
    public static final String SWAGGER_RESOURCES_URL = "/swagger-resources/";
    public static final String SWAGGER_RESOURCES_ALL_URL = "/swagger-resources/**";
    public static final String API_DOCS_URL = "/v3/api-docs";
    public static final String API_DOCS_ALL_URL = "/v3/api-docs/**";
    public static final String OAUTH2_URL = "/oauth2/**";

    public static final HttpMethod SIGNIN_METHOD = HttpMethod.POST;
    public static final String ACCESS_TOKEN = "access-token";

    public static final String ACCESS_COOKIE = "access-token";
    public static final String REFRESH_COOKIE = "refresh-token";

}
