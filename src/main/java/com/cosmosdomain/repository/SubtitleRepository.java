package com.cosmosdomain.repository;

import com.cosmosdomain.entity.Subtitle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubtitleRepository extends JpaRepository<Subtitle, Long> {
    List<Subtitle> findByLocalMediaId(Long localMediaId);
}