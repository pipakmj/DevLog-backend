package com.devlog.devlog.auth.repository;

import com.devlog.devlog.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    // 이메일로 유저 존재 여부 확인 (중복 가입 방지용)
    boolean existsByEmail(String email);

    // 이메일로 유저 정보 조회 (로그인 시 사용 예정)
    Optional<UserEntity> findByEmail(String email);
}
