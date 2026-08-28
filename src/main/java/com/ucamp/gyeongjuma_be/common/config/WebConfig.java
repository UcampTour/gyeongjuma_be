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

    /**
     * 허용할 프론트엔드 오리진. 배포 서버는 환경변수 CORS_ALLOWED_HOST로 주입한다.
     * 쉼표로 구분해 여러 개를 넣을 수 있다. (로컬 개발용 기본값 포함)
     */
    @Value("${cors.allowed.host}")
    private String[] allowedHosts;

    // 1. 인터셉터 설정
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1순위: 토큰 검증 후 memberId 주입
        registry.addInterceptor(authInterceptor)
                .order(1)
                .addPathPatterns("/api/members/**", "/api/mypage/**", "/api/quizzes/**",
                        "/api/place", "/api/visit/**", "/api/favorites/**", "/api/courses/**", "/api/admin/**")
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

    // 2. CORS — 리프레시 토큰 쿠키를 주고받으려면 allowCredentials(true)가 필요하고,
    //    이때 allowedOrigins에 와일드카드(*)를 쓸 수 없어 주소를 명시한다.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedHosts) // 서버 .env의 Vercel 주소가 여기에 들어감
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
