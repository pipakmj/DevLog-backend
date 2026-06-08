package com.devlog.devlog.auth.repository;

import com.devlog.devlog.auth.entity.TrendItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrendItemRepository extends JpaRepository<TrendItemEntity, Long> {
    Optional<TrendItemEntity> findByTypeAndOriginId(String type, String originId);

    @Modifying(clearAutomatically = true)
    @Query("update TrendItemEntity t set t.bookmarkCount = t.bookmarkCount + 1 where t.id = :id")
    void incrementBookmarkCount(Long id);

    @Modifying(clearAutomatically = true)
    @Query("update TrendItemEntity t set t.bookmarkCount = CASE WHEN t.bookmarkCount > 0 THEN t.bookmarkCount - 1 ELSE 0 END where t.id = :id")
    void decrementBookmarkCount(Long id);
}
