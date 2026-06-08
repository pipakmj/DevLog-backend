package com.devlog.devlog.auth.dto.bookmark;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkRequestDto {
    private String type;
    private String originId;
    private String title;
    private String url;
    private String thumbnailUrl;
    private int viewCount;
    private String metadata;
}
