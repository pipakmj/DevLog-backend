package com.devlog.devlog.global.common;

import com.devlog.devlog.auth.dto.portfolio.FeatureDTO;
import com.devlog.devlog.auth.dto.portfolio.PortfolioImageDTO;
import com.devlog.devlog.auth.dto.portfolio.TroubleshootDTO;
import com.devlog.devlog.auth.entity.PortfolioEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfGenerator {

    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    public byte[] generatePdf(PortfolioEntity portfolioEntity) throws JsonProcessingException {
        String html = generateHtml(portfolioEntity);
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(
                    () -> getClass().getResourceAsStream(
                            "/fonts/NotoSansKR-VariableFont_wght.ttf"
                    ),
                    "Noto Sans KR"
            );
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    public String generateHtml(PortfolioEntity portfolioEntity) throws JsonProcessingException {
        List<FeatureDTO> features =
                objectMapper.readValue(
                        portfolioEntity.getFeaturesJson(),
                        new TypeReference<List<FeatureDTO>>() {}
                );
        List<TroubleshootDTO> troubleshoots =
                objectMapper.readValue(
                        portfolioEntity.getTroubleshootsJson(),
                        new TypeReference<List<TroubleshootDTO>>() {}
                );
        List<String> techStacks =
                objectMapper.readValue(
                        portfolioEntity.getTechStackJson(),
                        new TypeReference<List<String>>() {}
                );
        PortfolioImageDTO images =
                objectMapper.readValue(
                        portfolioEntity.getImagesJson(),
                        PortfolioImageDTO.class
                );
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Context context = new Context();
        context.setVariable("projectName", portfolioEntity.getProject().getTitle());
        context.setVariable("generatedAt", portfolioEntity.getUpdatedAt().format(dateFormat));
        context.setVariable("projectPeriod", portfolioEntity.getProjectPeriod());
        context.setVariable("teamSize", portfolioEntity.getTeamSize());
        context.setVariable("primaryRole", portfolioEntity.getPrimaryRole());
        context.setVariable("techStackSummary", techStacks);
        context.setVariable("overview", portfolioEntity.getOverview());
        context.setVariable("roles", portfolioEntity.getRoles());
        context.setVariable("metrics", portfolioEntity.getMetrics());
        context.setVariable("techStacks", techStacks);
        context.setVariable("features", features);
        context.setVariable("troubleshoots", troubleshoots);
        context.setVariable("architectureImageUrl", images.getArchitecture().getImageUrl());
        context.setVariable("architectureDescription", images.getArchitecture().getDescription());
        context.setVariable("erdImageUrl", images.getErd().getImageUrl());
        context.setVariable("erdDescription", images.getErd().getDescription());
        context.setVariable("uiImages", images.getUi());
        return templateEngine.process("portfolio-pdf", context);
    }
}
