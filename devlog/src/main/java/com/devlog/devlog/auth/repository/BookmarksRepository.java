package com.devlog.devlog.auth.repository;

import com.devlog.devlog.auth.entity.BookmarksEntity;
import com.devlog.devlog.auth.entity.TrendItemEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarksRepository extends JpaRepository<BookmarksEntity, Long> {
    List<BookmarksEntity> findAllByUser(UserEntity user);

    Optional<BookmarksEntity> findByUserAndTrenditem(UserEntity user, TrendItemEntity trenditem);
}
