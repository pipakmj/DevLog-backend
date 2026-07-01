package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.entity.CommentEntity;
import com.devlog.devlog.auth.entity.PostEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.*;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 권한/보안 단위 테스트")
public class PostServiceTest {
    @InjectMocks
    private PostService postService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private StringRedisTemplate redisTemplate;

    @Nested
    @DisplayName("게시글 삭제 권한 테스트")
    class DeletePostService {
        @Test
        @DisplayName("실패: 타인의 게시글을 삭제하려 하면 UNAUTHORIZED_POST_ACCESS 예외가 발생한다")
        void deletePost_Fail_Unauthorized(){
            UserEntity owner = UserEntity.builder()
                    .email("owner@test.com")
                    .build();
            String otherEmail = "other@test.com";

            PostEntity post = PostEntity.builder()
                    .user(owner)
                    .build();
            when(postRepository.findById(1L)).thenReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.deletePost(otherEmail, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_POST_ACCESS);

            verify(postRepository, never()).delete(any());
        }

        @Test
        @DisplayName("성공: 본인의 게시글은 정상적으로 삭제된다")
        void deletePost_Success(){
            String ownerEmail = "owner@test.com";
            UserEntity owner = UserEntity.builder()
                    .email(ownerEmail)
                    .build();
            PostEntity post = PostEntity.builder()
                    .user(owner)
                    .build();

            when(postRepository.findById(1L)).thenReturn(Optional.of(post));
            postService.deletePost(ownerEmail, 1L);

            verify(postRepository, times(1)).delete(post);
        }
    }

    @Nested
    @DisplayName("댓글 삭제 권한 테스트")
    class DeleteCommentTest {
        @Test
        @DisplayName("실패: 타인의 댓글을 삭제하려 하면 UNAUTHORIZED_COMMENT_ACCESS 예외가 발생한다.")
        void deleteComment_Fail_Unauthorized(){
            UserEntity owner = UserEntity.builder()
                    .email("owner@test.com")
                    .build();
            String otherEmail = "other@test.com";

            CommentEntity comment = CommentEntity.builder()
                    .userEntity(owner)
                    .content("원본 댓글")
                    .isDeleted(false)
                    .build();

            when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

            assertThatThrownBy(() -> postService.deletePostComment(otherEmail, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
        }
    }
}
