package com.devlog.devlog.auth.repository;

import com.devlog.devlog.auth.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    // ProjectService 용 (무한스크롤)
    Slice<ProjectEntity> findByUserEntityId(int userId, Pageable pageable);

    List<ProjectEntity> findByUserEntityId(int userId);

    // PortfolioBuilderService 용 (전체 페이지 수 포함)
    @Query("SELECT p FROM ProjectEntity p WHERE p.userEntity.id = :userId")
    Page<ProjectEntity> findPageByUserEntityId(@Param("userId") int userId, Pageable pageable);
}
