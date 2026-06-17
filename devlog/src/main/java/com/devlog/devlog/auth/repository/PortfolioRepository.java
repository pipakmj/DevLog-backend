package com.devlog.devlog.auth.repository;

import com.devlog.devlog.auth.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioEntity, Long> {
}
