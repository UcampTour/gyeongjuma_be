package com.ucamp.gyeongjuma_be.common.config;

import com.ucamp.gyeongjuma_be.auth.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Value("${cors.allowed.host}")
    private String allowedHost;

    // 1. 인터셉터 설정 (기존 코드 유지)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/members/**", "/api/mypage/**", "/api/quizzes/**", "/api/place", "/api/visit/**")
                .excludePathPatterns(
                        "/api/members/login",
                        "/api/members/check-nickname",
                        "/api/members/reissue"
                );
    }

    // 2. CORS
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedHost) // 서버 .env의 Vercel 주소가 여기에 들어감
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}