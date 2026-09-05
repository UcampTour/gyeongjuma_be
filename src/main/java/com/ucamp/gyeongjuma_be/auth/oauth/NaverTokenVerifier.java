package com.ucamp.gyeongjuma_be.auth.oauth;

import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Naver access_token 검증기.
 * 프론트에서 받은 access_token으로 네이버 회원 프로필 API를 호출한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverTokenVerifier implements SocialTokenVerifier {

    private static final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    private final RestClient restClient;

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
        } catch (RestClientResponseException e) {
            // 어떤 이유로 거절됐는지 알아야 프론트 문제인지 앱 설정 문제인지 가려낼 수 있다
            log.error("Naver access_token 검증 실패. status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        } catch (RestClientException e) {
            log.error("Naver 서버 호출 실패(네트워크·타임아웃): {}", e.getMessage());
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        if (body == null || !"00".equals(body.get("resultcode"))) {
            // 네이버는 200으로 응답하면서 resultcode로 실패를 알리는 경우가 있다
            log.error("Naver 응답이 정상이 아님. resultcode={} message={}",
                    body == null ? null : body.get("resultcode"),
                    body == null ? null : body.get("message"));
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        Object responseObj = body.get("response");
        if (!(responseObj instanceof Map)) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }

        Map<String, Object> response = (Map<String, Object>) responseObj;
        String providerId = (String) response.get("id");
        if (providerId == null) {
            // 네이버 앱에 "회원 식별자" 제공 항목이 설정되지 않으면 id가 비어서 온다
            log.error("Naver 응답에 id가 없음. 네이버 개발자센터의 제공 항목 설정 확인 필요. keys={}",
                    response.keySet());
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
