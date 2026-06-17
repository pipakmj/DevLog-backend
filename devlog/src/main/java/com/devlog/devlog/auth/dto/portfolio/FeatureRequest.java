package com.devlog.devlog.auth.dto.portfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureRequest {
    private String title;
    private String description;
}
