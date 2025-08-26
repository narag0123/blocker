package com.jude.blocker.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



/**
 * 전역 CORS 설정
 * - 컨트롤러에 @CrossOrigin 붙일 필요 없음
 * - 기본: http://localhost:3000 허용 (Next.js dev)
 * - 필요 시 application.properties에서 변경 가능
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    // 쉼표로 여러 Origin 지정 가능: http://localhost:3000,http://127.0.0.1:3000
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(split(allowedOrigins))
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE","OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false) // 쿠키/인증 필요하면 true로 (서버/프론트 모두 설정 필요)
            .maxAge(3600);           // Preflight 결과 캐시(초)
    }

    private String[] split(String s) {
        return s == null ? new String[0] :
            s.replace(" ", "").split(",");
    }

}
