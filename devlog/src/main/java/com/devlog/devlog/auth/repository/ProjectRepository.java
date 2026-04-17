package com.devlog.devlog.auth.repository;

import com.devlog.devlog.auth.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Slice<ProjectEntity> findByUserEntityId(int userId, Pageable pageable);
}
