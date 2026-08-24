package com.ucamp.gyeongjuma_be.favorite.controller;

import com.ucamp.gyeongjuma_be.auth.AuthInterceptor;
import com.ucamp.gyeongjuma_be.common.dto.ApiResponse;
import com.ucamp.gyeongjuma_be.favorite.dto.FavoriteResponse;
import com.ucamp.gyeongjuma_be.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/{placeId}")
    public ResponseEntity<ApiResponse<FavoriteResponse>> toggleFavorite(
            @PathVariable Long placeId,
            @RequestAttribute(AuthInterceptor.MEMBER_ID_ATTRIBUTE) Long memberId
    ) {
        FavoriteResponse response = favoriteService.toggleFavorite(memberId, placeId);
        String message = response.isFavorite() ? "즐겨찾기를 추가했습니다." : "즐겨찾기를 해제했습니다.";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}
