package com.innobridge.ethpay.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import java.util.Collections;

import static com.innobridge.ethpay.constants.HTTPConstants.*;

@Configuration
public class OpenApiConfig implements WebMvcConfigurer {
    private static final String API_TITLE = "Payment API";
    private static final String API_VERSION = "1.0";
    private static final String API_DESCRIPTION = "API for Payment";
    private static final String BEARER_ACCESS_TOKEN_SCHEMA = "BearerAccessTokenSchema";
    private static final String COOKIE_ACCESS_TOKEN_SCHEMA = "CookieAccessTokenSchema";
    private static final String BEARER_ACCESS_TOKEN_FORMAT = "JWT";
    private static final String BEARER = "Bearer";
  @Bean
  public OpenAPI customOpenAPI() {
      return new OpenAPI()
              .info(new Info()
                      .title(API_TITLE)
                      .version(API_VERSION)
                      .description(API_DESCRIPTION))
              .components(new Components()
                      /**
                       * Add jwt access token to header when we call API from swagger UI
                       */
                      .addSecuritySchemes(BEARER_ACCESS_TOKEN_SCHEMA, new SecurityScheme()
                              .name(ACCESS_TOKEN)
                              .type(SecurityScheme.Type.HTTP)
                              .bearerFormat(BEARER_ACCESS_TOKEN_FORMAT)
                              .in(SecurityScheme.In.HEADER)
                              .scheme(BEARER))
                      /**
                       * Adding cookies when calling API from swagger UI is currently not supported
                       * reference https://github.com/swagger-api/swagger-js/issues/1163
                       */
                      .addSecuritySchemes(COOKIE_ACCESS_TOKEN_SCHEMA, new SecurityScheme()
                              .name(ACCESS_COOKIE)
                              .type(SecurityScheme.Type.APIKEY)
                              .in(SecurityScheme.In.COOKIE)
                              .name(ACCESS_COOKIE)
                              .description("Cookie parameter authorization/authentication is not supported for swagger reference https://github.com/swagger-api/swagger-js/issues/1163")))
              .security(Collections.singletonList(new SecurityRequirement().addList(BEARER_ACCESS_TOKEN_SCHEMA)
                      .addList(COOKIE_ACCESS_TOKEN_SCHEMA)));
  }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html**").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}