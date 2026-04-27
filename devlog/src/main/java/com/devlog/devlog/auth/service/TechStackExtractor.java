package com.devlog.devlog.auth.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TechStackExtractor {
    public List<String> extractTechStack(List<String> rootFiles) {
        List<String> techStack = new ArrayList<>();
        for (String rootFile : rootFiles) {
            switch (rootFile) {
                // Java
                case "build.gradle", "build.gradle.kts" -> addIfAbsent(techStack, "Java", "Spring Boot", "Gradle");
                case "pom.xml" -> addIfAbsent(techStack, "Java", "Spring Boot", "Maven");
                // JavaScript / TypeScript
                case "package.json" -> addIfAbsent(techStack, "Node.js");
                case "tsconfig.json" -> addIfAbsent(techStack, "TypeScript");
                case "next.config.js", "next.config.mjs", "next.config.ts" -> addIfAbsent(techStack, "Next.js");
                case "vite.config.js", "vite.config.ts" -> addIfAbsent(techStack, "Vite");
                case "nuxt.config.js", "nuxt.config.ts" -> addIfAbsent(techStack, "Nuxt.js");
                case "angular.json" -> addIfAbsent(techStack, "Angular");
                // Python
                case "requirements.txt", "setup.py", "pyproject.toml" -> addIfAbsent(techStack, "Python");
                case "manage.py" -> addIfAbsent(techStack, "Python", "Django");
                // Go
                case "go.mod" -> addIfAbsent(techStack, "Go");
                // Rust
                case "Cargo.toml" -> addIfAbsent(techStack, "Rust");
                // DevOps / Infra
                case "Dockerfile" -> addIfAbsent(techStack, "Docker");
                case "docker-compose.yml", "docker-compose.yaml" -> addIfAbsent(techStack, "Docker Compose");
                case ".github" -> addIfAbsent(techStack, "GitHub Actions");
                // Database
                case "redis.conf" -> addIfAbsent(techStack, "Redis");
            }
        }
        return techStack;
    }

    // 중복 방지 헬퍼 메서드
    private void addIfAbsent(List<String> list, String... items) {
        for (String item : items) {
            if (!list.contains(item)) {
                list.add(item);
            }
        }
    }
}
