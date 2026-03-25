package com.devlog.devlog;

import com.devlog.devlog.global.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/auth/login-test")
    public String loginTest(@RequestParam String id) {
        return jwtTokenProvider.createAccessToken(id);
    }
    @GetMapping("/api/hello")
    public String hello() {
        return "hello";
    }
}
