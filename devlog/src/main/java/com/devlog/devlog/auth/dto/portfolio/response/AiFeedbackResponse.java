package com.devlog.devlog.auth.dto.portfolio.response;

import com.devlog.devlog.auth.dto.portfolio.AutoCompletedFieldsDTO;
import com.devlog.devlog.global.common.UsageLimitResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFeedbackResponse {
    private int score;
    private List<String> missingSections;
    private List<String> suggestions;
    private AutoCompletedFieldsDTO autoCompletedFields;
    private UsageLimitResponse usageLimit;
}
