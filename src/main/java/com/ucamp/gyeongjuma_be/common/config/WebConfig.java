package com.ucamp.gyeongjuma_be.common.config;

import com.ucamp.gyeongjuma_be.auth.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/members/**")
                .excludePathPatterns(
                        "/api/members/login",
                        "/api/members/check-nickname",
                        "/api/members/reissue"
                );
    }
}
