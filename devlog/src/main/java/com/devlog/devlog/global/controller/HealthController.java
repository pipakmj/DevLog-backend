package com.devlog.devlog.global.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    //render 서버를 유지하기 위한 체크용 api
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}
