package com.cosmosdomain.repository;

import com.cosmosdomain.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndMediaTypeAndMediaId(
        Long userId, Favorite.MediaType mediaType, Long mediaId
    );
    void deleteByUserIdAndMediaTypeAndMediaId(
        Long userId, Favorite.MediaType mediaType, Long mediaId
    );
}