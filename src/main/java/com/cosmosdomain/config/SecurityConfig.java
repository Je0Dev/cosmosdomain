package com.cosmosdomain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 *
 * <p>Exposes the login/register pages and static assets, protects every other
 * route, and uses the session-based form login flow backed by
 * {@link com.cosmosdomain.service.AuthService}.</p>
 */
@Configuration
@EnableWebSecurity // Registers the Spring Security filter chain.
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF is disabled because the UI uses plain HTML forms and a small
            // fetch-based favorite toggle. Re-enable this before production use.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Public pages and development tooling.
                .requestMatchers("/", "/login", "/register", "/error", "/h2-console/**").permitAll()
                // Static assets must be reachable before login.
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // Everything else (catalog, search, favorites) requires a session.
                .anyRequest().authenticated()
            )
            // Spring Security handles the POST to /login; this controller only
            // renders the form.
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
            )
            // A session is created on demand rather than for every request.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}