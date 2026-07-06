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
 * Kakao access_token 검증기.
 * 프론트에서 받은 access_token으로 카카오 사용자 정보 API를 호출한다.
 */
@Slf4j
@Component
public class KakaoTokenVerifier implements SocialTokenVerifier {

    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient = RestClient.create();

    @Override
    public String provider() {
        return "KAKAO";
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
            log.error("Kakao access_token 검증 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        if (body == null || body.get("id") == null) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        String providerId = String.valueOf(body.get("id"));
        String email = null;
        String name = null;
        String profileImage = null;

        Object accountObj = body.get("kakao_account");
        if (accountObj instanceof Map) {
            Map<String, Object> account = (Map<String, Object>) accountObj;
            email = (String) account.get("email");
            Object profileObj = account.get("profile");
            if (profileObj instanceof Map) {
                Map<String, Object> profile = (Map<String, Object>) profileObj;
                name = (String) profile.get("nickname");
                profileImage = (String) profile.get("profile_image_url");
            }
        }

        return new SocialUserInfo(providerId, email, name, profileImage);
    }
}
