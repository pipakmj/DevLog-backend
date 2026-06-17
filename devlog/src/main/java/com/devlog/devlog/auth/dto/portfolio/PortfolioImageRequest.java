package com.devlog.devlog.auth.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioImageRequest {
    private ImageRequest architecture;
    private ImageRequest erd;
    private List<ImageRequest> ui;
}
