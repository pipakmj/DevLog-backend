package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.post.*;
import com.devlog.devlog.auth.entity.*;
import com.devlog.devlog.auth.repository.*;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getDetailPost(Long postId) {
        return postRepository.findById(postId)
                .map(PostDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
    @Transactional
    public void updatePostViewCount(Long postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        int updatedViews = postEntity.getViews() + 1;
        postEntity.setViews(updatedViews);
    }

    @Transactional
    public void createPost(String userEmail, PostRequest postRequest) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProjectEntity project = projectRepository.findById(postRequest.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (project.getUserEntity().getId() != user.getId()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);
        }

        PostEntity postEntity = PostEntity.builder()
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .user(user)
                .project(project)
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .views(0)
                .build();

        if (postRequest.getTags() != null && !postRequest.getTags().isEmpty()) {
            Set<TagEntity> tagEntities = Arrays.stream(postRequest.getTags().split(","))
                    .map(String::trim)
                    .filter(tagName -> !tagName.isEmpty())
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(TagEntity.builder().name(tagName).build())))
                    .collect(Collectors.toSet());
            postEntity.setTags(tagEntities);
        }

        postRepository.save(postEntity);
    }

    @Transactional
    public void updatePost(String userEmail,Long postId, PostRequest postRequest) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if(postEntity.getUser().getId() != user.getId()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_POST_ACCESS);
        }

        postEntity.setTitle(postRequest.getTitle());
        postEntity.setContent(postRequest.getContent());
        postEntity.setUpdated_at(LocalDateTime.now());

        //프로젝트 변경 처리
        if(postRequest.getProjectId() != null) {
            ProjectEntity project = projectRepository.findById(postRequest.getProjectId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
            //변경하려면 프로젝트도 본인 소유여야함
            if(project.getUserEntity().getId() != user.getId()) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);
            }
            postEntity.setProject(project);
        }
        // 태그를 초기화 후 addAll()하는 방식이 기존의 연관관계테이블의 데이터가 깔끔하게 교체하는 안전한 방법
        if(postRequest.getTags() != null){
            Set<TagEntity> tagEntities = Arrays.stream(postRequest.getTags().split(","))
                    .map(String::trim)
                    .filter(tagName -> !tagName.isEmpty())
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(TagEntity.builder().name(tagName).build())))
                    .collect(Collectors.toSet());

            postEntity.getTags().clear();
            postEntity.getTags().addAll(tagEntities);
        }
        //@Transactional 안에서 엔티티를 조회하고 필드값을 변경하면 메서드가 끝날때 Hibernate가 변경을 감지하여 자동으로 update 쿼리를 날려주기 때문에
        //별도의 postRespository.save()를 호출할 필요가 없음
    }

    @Transactional
    public void deletePost(String userEmail, Long postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if(!postEntity.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_POST_ACCESS);
        }

        postRepository.delete(postEntity);
    }

    @Transactional
    public LikesResponse likePost(String userEmail, Long postId) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        Optional<LikeEntity> existingLike = likeRepository.findByUserEntityAndPostEntity(user, postEntity);
        if(existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        }else{
            LikeEntity likeEntity = LikeEntity.builder()
                    .userEntity(user)
                    .postEntity(postEntity)
                    .createdAt(LocalDateTime.now())
                    .build();
            likeRepository.save(likeEntity);
        }
        return likePostStatus(userEmail, postId);
    }

    @Transactional(readOnly = true)
    public LikesResponse likePostStatus(String userEmail, Long postId) {
        int likeCount = likeRepository.countByPostEntityId(postId);

        if (userEmail == null) {
            return LikesResponse.builder()
                    .likeCount(likeCount)
                    .isLiked(false)
                    .build();
        }
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isLiked = likeRepository.existsByUserEntityIdAndPostEntityId(user.getId(), postId);
        return LikesResponse.builder()
                .likeCount(likeCount)
                .isLiked(isLiked)
                .build();
    }

    @Transactional
    public void createPostComment(String userEmail, Long postId, CommentRequest commentRequest) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        CommentEntity commentEntity = CommentEntity.builder()
                .content(commentRequest.getContent())
                .parentId(commentRequest.getParentId())
                .createdAt(LocalDateTime.now())
                .user(user)
                .post(postEntity)
                .build();
        commentRepository.save(commentEntity);
    }
}
