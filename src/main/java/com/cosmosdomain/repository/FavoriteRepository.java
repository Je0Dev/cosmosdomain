package com.cosmosdomain.repository;

import com.cosmosdomain.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link Favorite} rows.
 *
 * <p>Derived queries cover listing a user's favorites and looking up or
 * deleting the single row for a given user/type/media id.</p>
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    // All favorites belonging to one user (used by the favorites page).
    List<Favorite> findByUserId(Long userId);

    // Exact lookup used by the toggle logic to detect an existing favorite.
    Optional<Favorite> findByUserIdAndMediaTypeAndMediaId(
        Long userId, Favorite.MediaType mediaType, Long mediaId
    );

    // Bulk delete variant; kept for callers that do not need the entity first.
    void deleteByUserIdAndMediaTypeAndMediaId(
        Long userId, Favorite.MediaType mediaType, Long mediaId
    );
}