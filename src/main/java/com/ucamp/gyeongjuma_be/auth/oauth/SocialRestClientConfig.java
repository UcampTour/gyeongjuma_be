package com.ucamp.gyeongjuma_be.auth.oauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * 소셜 로그인 검증용 공용 RestClient.
 *
 * 타임아웃을 주지 않으면 소셜 서버가 응답하지 않을 때 스레드가 무한정 대기하고,
 * 그동안 잡고 있던 DB 커넥션도 반납되지 않아 커넥션 풀이 고갈된다.
 */
@Configuration
public class SocialRestClientConfig {

    @Bean
    public RestClient socialRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
