package com.cosmosdomain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Shared infrastructure beans used across the application.
 *
 * <p>Defines the JDK-based HTTP client used to call TMDB and the BCrypt
 * password encoder used by authentication and user registration.</p>
 */
@Configuration
public class ApplicationConfig {

    // Uses the JDK HttpClient instead of an extra Apache dependency.
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder().requestFactory(new JdkClientHttpRequestFactory());
    }

    // Shared client injected into TmdbClient.
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

    // BCrypt is the password hashing algorithm used for stored credentials.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}