package com.cosmosdomain.repository;

import com.cosmosdomain.entity.LocalMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocalMediaRepository extends JpaRepository<LocalMedia, Long> {
    List<LocalMedia> findByUserIdOrderByAddedAtDesc(Long userId);
}