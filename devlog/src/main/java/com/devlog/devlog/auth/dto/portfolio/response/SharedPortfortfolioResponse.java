package com.devlog.devlog.auth.dto.portfolio.response;

import com.devlog.devlog.auth.dto.portfolio.request.FeatureDTO;
import com.devlog.devlog.auth.dto.portfolio.request.PortfolioImageDTO;
import com.devlog.devlog.auth.dto.portfolio.request.TroubleshootDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SharedPortfortfolioResponse {
    private Long id;
    private String projectName;
    private String overview;
    private String roles;
    private List<String> techStack;
    private List<FeatureDTO> features;
    private List<TroubleshootDTO> troubleshoots;
    private String metrics;
    private PortfolioImageDTO images;
    private LocalDateTime updatedAt;
}
