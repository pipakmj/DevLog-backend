package com.devlog.devlog.auth.repository;

import com.devlog.devlog.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByEmail(String email);
    void deleteByEmail(String email);
    Optional<RefreshTokenEntity> findByToken(String token);
}
