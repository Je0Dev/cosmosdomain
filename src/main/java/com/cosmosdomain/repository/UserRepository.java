package com.cosmosdomain.repository;

import com.cosmosdomain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Spring Data repository for {@link User} rows.
 *
 * <p>Provides the username/email lookups needed by authentication, registration
 * uniqueness checks, and current-user resolution in controllers.</p>
 */
public interface UserRepository extends JpaRepository<User, Long> {
    // Used by authentication and by controllers resolving the current user.
    Optional<User> findByUsername(String username);

    // Email lookup is available for future account-recovery flows.
    Optional<User> findByEmail(String email);

    // Registration uniqueness checks.
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}