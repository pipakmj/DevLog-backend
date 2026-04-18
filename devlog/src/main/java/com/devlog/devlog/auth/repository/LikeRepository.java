package com.devlog.devlog.auth.repository;

import com.devlog.devlog.auth.entity.LikeEntity;
import com.devlog.devlog.auth.entity.PostEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    Optional<LikeEntity> findByUserEntityAndPostEntity(UserEntity user, PostEntity post);
    int countByPostEntity_Id(Long postEntityId);
    boolean existsByUserEntity_IdAndPostEntity_Id(int userEntityId, Long postEntityId);

    int countByPostEntityId(Long postId);

    boolean existsByUserEntityIdAndPostEntityId(int id, Long postId);
}
