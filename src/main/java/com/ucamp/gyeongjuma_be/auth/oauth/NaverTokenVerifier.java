package com.ucamp.gyeongjuma_be.auth.oauth;

import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Naver access_token 검증기.
 * 프론트에서 받은 access_token으로 네이버 회원 프로필 API를 호출한다.
 */
@Slf4j
@Component
public class NaverTokenVerifier implements SocialTokenVerifier {

    private static final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    private final RestClient restClient = RestClient.create();

    @Override
    public String provider() {
        return "NAVER";
    }

    @Override
    @SuppressWarnings("unchecked")
    public SocialUserInfo verify(String accessToken) {
        Map<String, Object> body;
        try {
            body = restClient.get()
                    .uri(USER_INFO_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (RestClientException e) {
            log.error("Naver access_token 검증 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        if (body == null || !"00".equals(body.get("resultcode"))) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        Object responseObj = body.get("response");
        if (!(responseObj instanceof Map)) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        Map<String, Object> response = (Map<String, Object>) responseObj;
        String providerId = (String) response.get("id");
        if (providerId == null) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        return new SocialUserInfo(
                providerId,
                (String) response.get("email"),
                (String) response.get("nickname"),
                (String) response.get("profile_image")
        );
    }
}
