package com.sekhmet.llmflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Sekhmet LLM Flow 后端启动入口类
 * 配置了 Spring Boot 应用和异步处理能力
 */
@SpringBootApplication
@EnableAsync
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
