package com.devlog.devlog.auth.dto.portfolio.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PdfDownloadResponse {
    private String fileName;
    private byte[] pdfBytes;
}
