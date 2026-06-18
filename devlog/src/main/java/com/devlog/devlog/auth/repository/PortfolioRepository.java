package com.devlog.devlog.auth.repository;

import com.devlog.devlog.auth.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioEntity, Long> {
    Optional<PortfolioEntity> findByProjectId(Long projectId);
    Optional<PortfolioEntity> findByShareToken(String shareToken);
}
