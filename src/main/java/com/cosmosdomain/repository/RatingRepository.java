package com.cosmosdomain.repository;

import com.cosmosdomain.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndMediaTypeAndMediaId(
        Long userId, Rating.MediaType mediaType, Long mediaId
    );
    void deleteByUserIdAndMediaTypeAndMediaId(
        Long userId, Rating.MediaType mediaType, Long mediaId
    );
}