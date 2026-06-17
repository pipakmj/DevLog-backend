package com.devlog.devlog.auth.dto.portfolio.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioImageDTO {
    private ImageDTO architecture;
    private ImageDTO erd;
    private List<ImageDTO> ui;
}
