package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.bookmark.BookmarkRequestDto;
import com.devlog.devlog.auth.dto.bookmark.BookmarkResponseDto;
import com.devlog.devlog.auth.entity.BookmarksEntity;
import com.devlog.devlog.auth.entity.TrendItemEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.BookmarksRepository;
import com.devlog.devlog.auth.repository.TrendItemRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

        private final BookmarksRepository bookmarksRepository;
        private final TrendItemRepository trendItemRepository;
        private final UserRepository userRepository;

        @Transactional(readOnly = true)
        public List<BookmarkResponseDto> getBookmarks(String email) {
                UserEntity user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                return bookmarksRepository.findAllByUser(user).stream()
                                .map(BookmarkResponseDto::from)
                                .collect(Collectors.toList());
        }

        @Transactional
        public BookmarkResponseDto addBookmark(String email, BookmarkRequestDto request) {
                UserEntity user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                TrendItemEntity trendItem = trendItemRepository
                                .findByTypeAndOriginId(request.getType(), request.getOriginId())
                                .orElseGet(() -> trendItemRepository.save(TrendItemEntity.builder()
                                                .type(request.getType())
                                                .originId(request.getOriginId())
                                                .title(request.getTitle())
                                                .url(request.getUrl())
                                                .thumbnailUrl(request.getThumbnailUrl())
                                                .viewCount(request.getViewCount())
                                                .bookmarkCount(0)
                                                .metadata(request.getMetadata())
                                                .build()));

                return bookmarksRepository.findByUserAndTrenditem(user, trendItem)
                                .map(BookmarkResponseDto::from)
                                .orElseGet(() -> {
                                        BookmarksEntity bookmark = BookmarksEntity.builder()
                                                        .user(user)
                                                        .trenditem(trendItem)
                                                        .build();

                                        bookmark = bookmarksRepository.save(bookmark);
                                        trendItemRepository.incrementBookmarkCount(trendItem.getId());

                                        // UI 응답을 위해 현재 객체의 카운트도 1 증가시킴 (DB는 이미 업데이트됨)
                                        trendItem.setBookmarkCount(trendItem.getBookmarkCount() + 1);
                                        return BookmarkResponseDto.from(bookmark);
                                });
        }

        @Transactional
        public void deleteBookmark(String email, Long bookmarkId) {
                UserEntity user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                BookmarksEntity bookmark = bookmarksRepository.findById(bookmarkId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND));

                if (bookmark.getUser().getId() != user.getId()) {
                        throw new BusinessException(ErrorCode.UNAUTHORIZED_BOOKMARK_ACCESS);
                }

                TrendItemEntity trendItem = bookmark.getTrenditem();
                bookmarksRepository.delete(bookmark);
                trendItemRepository.decrementBookmarkCount(trendItem.getId());
        }
}
