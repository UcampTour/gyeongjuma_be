package com.ucamp.gyeongjuma_be.auth.oauth;

import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Google id_token 검증기.
 * 프론트에서 받은 id_token을 Google tokeninfo 엔드포인트로 검증한다.
 */
@Slf4j
@Component
public class GoogleTokenVerifier implements SocialTokenVerifier {

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}";

    private final RestClient restClient = RestClient.create();

    @Value("${oauth.google.client-id:}")
    private String clientId;

    @Override
    public String provider() {
        return "GOOGLE";
    }

    @Override
    public SocialUserInfo verify(String idToken) {
        Map<String, Object> body;
        try {
            body = restClient.get()
                    .uri(TOKEN_INFO_URL, idToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (RestClientException e) {
            log.error("Google id_token 검증 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        if (body == null || body.get("sub") == null) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        // client-id가 설정되어 있으면 aud 일치 여부 검증
        if (clientId != null && !clientId.isBlank() && !clientId.equals(body.get("aud"))) {
            log.error("Google id_token aud 불일치. aud={}", body.get("aud"));
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        return new SocialUserInfo(
                (String) body.get("sub"),
                (String) body.get("email"),
                (String) body.get("name"),
                (String) body.get("picture")
        );
    }
}
