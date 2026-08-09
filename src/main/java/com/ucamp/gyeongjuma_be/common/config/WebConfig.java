package com.ucamp.gyeongjuma_be.common.config;

import com.ucamp.gyeongjuma_be.auth.AdminInterceptor;
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
    private final AdminInterceptor adminInterceptor;

    /** 허용할 프론트엔드 오리진 (개발/배포 주소를 properties에서 관리) */
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1순위: 토큰 검증 후 memberId 주입
        registry.addInterceptor(authInterceptor)
                .order(1)
                .addPathPatterns("/api/members/**", "/api/mypage/**", "/api/admin/**")
                .excludePathPatterns(
                        "/api/members/login",
                        "/api/members/check-nickname",
                        "/api/members/reissue",
                        "/api/admin/login"
                );

        // 2순위: 관리자 권한 검사 (memberId가 주입된 뒤에 실행되어야 함)
        registry.addInterceptor(adminInterceptor)
                .order(2)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login");
    }

    /**
     * 리프레시 토큰 쿠키를 주고받으려면 allowCredentials(true)가 필요하고,
     * 이때 allowedOrigins에 와일드카드(*)를 쓸 수 없어 주소를 명시한다.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
