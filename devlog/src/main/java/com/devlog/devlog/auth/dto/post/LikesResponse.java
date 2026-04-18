package com.devlog.devlog.auth.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikesResponse {
    @JsonProperty("isLiked")
    private boolean isLiked;
    private int likeCount;
}
