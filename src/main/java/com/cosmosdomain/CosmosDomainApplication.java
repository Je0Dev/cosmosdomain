package com.cosmosdomain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Application entry point for CosmosDomain.
 *
 * <p>Boots the Spring context, enables Spring Cache for TMDB responses, and
 * disables the two client autoconfigurations that would otherwise create a
 * second, competing {@code RestClient} bean (this project builds its own in
 * {@link com.cosmosdomain.config.ApplicationConfig}).</p>
 */
// These two autoconfigurations are excluded because ApplicationConfig builds
// and exposes the single RestClient used by TmdbClient.
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
})
@EnableCaching // Enables the @Cacheable annotations on TmdbClient.
public class CosmosDomainApplication {

    public static void main(String[] args) {
        SpringApplication.run(CosmosDomainApplication.class, args);
    }
}