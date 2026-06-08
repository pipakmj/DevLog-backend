package com.devlog.devlog.auth.dto.bookmark;

import com.devlog.devlog.auth.entity.BookmarksEntity;
import com.devlog.devlog.auth.entity.TrendItemEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookmarkResponseDto {
    private Long id;
    private LocalDateTime createdAt;
    private TrendItemDto item;

    @Getter
    @Builder
    public static class TrendItemDto {
        private Long id; // Bookmark PK for deletion
        private Long trendItemId; // Original TrendItem PK
        private String type;
        private String originId;
        private String title;
        private String url;
        private String thumbnailUrl;
        private int viewCount;
        private int bookmarkCount;
        private String metadata;
    }

    public static BookmarkResponseDto from(BookmarksEntity entity) {
        TrendItemEntity item = entity.getTrenditem();
        return BookmarkResponseDto.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .item(TrendItemDto.builder()
                        .id(entity.getId()) // Set to Bookmark PK
                        .trendItemId(item.getId())
                        .type(item.getType())
                        .originId(item.getOriginId())
                        .title(item.getTitle())
                        .url(item.getUrl())
                        .thumbnailUrl(item.getThumbnailUrl())
                        .viewCount(item.getViewCount())
                        .bookmarkCount(item.getBookmarkCount())
                        .metadata(item.getMetadata())
                        .build())
                .build();
    }
}
