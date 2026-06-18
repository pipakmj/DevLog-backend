package com.devlog.devlog.auth.dto.portfolio.response;

import com.devlog.devlog.auth.dto.portfolio.AutoCompletedFieldsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFeedbackResponse {
    private int score;
    private List<String> missingSections;
    private List<String> suggestions;
    private AutoCompletedFieldsDTO autoCompletedFields;
}
