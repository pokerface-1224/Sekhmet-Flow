package com.sekhmet.llmflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

/**
 * 跨域资源共享 (CORS) 配置类
 * 允许前端应用访问后端 API
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    /**
     * 配置跨域映射规则
     * 允许所有路径、所有来源、所有方法和所有头部信息
     * @param registry CORS 注册表
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
