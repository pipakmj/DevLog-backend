package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.PostDetailResponse;
import com.devlog.devlog.auth.dto.PostRequest;
import com.devlog.devlog.auth.dto.PostResponse;
import com.devlog.devlog.auth.entity.PostEntity;
import com.devlog.devlog.auth.entity.ProjectEntity;
import com.devlog.devlog.auth.entity.TagEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.PostRepository;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.repository.TagRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
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
    public void deletePost(String userEmail, Long postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if(!postEntity.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_POST_ACCESS);
        }

        postRepository.delete(postEntity);
    }
}
