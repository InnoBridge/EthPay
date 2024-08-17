package com.innobridge.ethpay.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Slf4j
@Configuration
public class ExternalClientConfiguration {

    @Bean
    public MongoClient mongoClient(@Value("${MONGO_DATABASE_URI:}") String mongoDatabaseUri) {
        if (mongoDatabaseUri == null || mongoDatabaseUri.isEmpty()) {
            String errorMessage = "MONGO_DATABASE_URI not set in the environment variables";
            log.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }}, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Failed to initialize SSL context", e);
            throw new RuntimeException("Failed to initialize SSL context", e);
        }

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoDatabaseUri))
                .applyToSslSettings(builder -> builder.enabled(true).context(sslContext))
                .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
                .build();

        return MongoClients.create(settings);
    }
}