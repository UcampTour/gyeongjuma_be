package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.PlaceContentUpsertRequest;
import com.ucamp.gyeongjuma_be.admin.dto.request.PlaceCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceContentDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceItemDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceItemListResponse;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceListResponse;
import com.ucamp.gyeongjuma_be.admin.repository.AdminPlaceRepository;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPlaceServiceImpl implements AdminPlaceService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AdminPlaceRepository adminPlaceRepository;

    @Override
    public AdminPlaceListResponse getPlaces(String keyword, Boolean isActive, int page, int size) {
        int offset = page * size;
        List<AdminPlaceDto> places = adminPlaceRepository.findPlaces(keyword, isActive, offset, size);
        long totalCnt = adminPlaceRepository.countPlaces(keyword, isActive);

        return AdminPlaceListResponse.builder()
                .totalCnt(totalCnt)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) totalCnt / size))
                .places(places)
                .build();
    }

    @Override
    @Transactional
    public AdminPlaceDto createPlace(PlaceCreateRequest request) {
        adminPlaceRepository.insertPlace(request, LocalDateTime.now(KST));
        Long placeId = adminPlaceRepository.findLastInsertedPlaceId();
        return adminPlaceRepository.findPlaceById(placeId);
    }

    @Override
    @Transactional
    public void deletePlace(Long placeId) {
        AdminPlaceDto place = getExistingPlace(placeId);
        if (Boolean.FALSE.equals(place.getIsActive())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        adminPlaceRepository.softDeletePlace(placeId, LocalDateTime.now(KST));
    }

    @Override
    @Transactional
    public AdminPlaceDto restorePlace(Long placeId) {
        AdminPlaceDto place = getExistingPlace(placeId);
        if (Boolean.TRUE.equals(place.getIsActive())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        adminPlaceRepository.restorePlace(placeId, LocalDateTime.now(KST));
        return adminPlaceRepository.findPlaceById(placeId);
    }

    @Override
    public AdminPlaceItemListResponse getPlaceItems(String keyword, Boolean isActive, int page, int size) {
        int offset = page * size;
        List<AdminPlaceItemDto> places = adminPlaceRepository.findPlaceItems(keyword, isActive, offset, size);
        attachContents(places);
        long totalCnt = adminPlaceRepository.countPlaces(keyword, isActive);

        return AdminPlaceItemListResponse.builder()
                .totalCnt(totalCnt)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) totalCnt / size))
                .places(places)
                .build();
    }

    @Override
    public AdminPlaceItemDto getPlaceItem(Long placeId) {
        AdminPlaceItemDto place = adminPlaceRepository.findPlaceItemById(placeId);
        if (place == null) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        attachContents(List.of(place));
        return place;
    }

    /**
     * 해설 등록·수정. 같은 (언어, 난이도) 해설이 있으면 내용만 교체한다.
     * place_content에 유니크 제약이 없어 UPDATE를 먼저 시도하고 대상이 없을 때만 INSERT 한다.
     */
    @Override
    @Transactional
    public AdminPlaceItemDto upsertContent(Long placeId, PlaceContentUpsertRequest request) {
        getExistingPlace(placeId);

        String language = request.normalizedLanguage();
        String difficulty = request.normalizedDifficulty();

        int updated = adminPlaceRepository.upsertPlaceContent(
                placeId, language, difficulty, request.description());
        if (updated == 0) {
            adminPlaceRepository.insertPlaceContent(placeId, language, difficulty, request.description());
        }

        return getPlaceItem(placeId);
    }

    @Override
    @Transactional
    public void deleteContent(Long placeId, Long placeContentId) {
        getExistingPlace(placeId);

        AdminPlaceContentDto content = adminPlaceRepository.findContentById(placeContentId);
        // 다른 관광지의 해설을 지우지 못하게 소속을 확인한다.
        // 조회가 그룹 대표 ID를 돌려주므로, 어느 언어판 ID로 요청했든 같은 그룹인지로 본다.
        AdminPlaceItemDto place = adminPlaceRepository.findPlaceItemById(placeId);
        if (content == null || place == null || !place.getPlaceId().equals(content.getPlaceId())) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        adminPlaceRepository.deletePlaceContent(placeContentId);
    }

    /** 관광지 목록에 해설을 한 번의 조회로 묶어 넣는다 (N+1 회피) */
    private void attachContents(List<AdminPlaceItemDto> places) {
        if (places.isEmpty()) {
            return;
        }
        List<Long> placeIds = places.stream().map(AdminPlaceItemDto::getPlaceId).toList();
        Map<Long, List<AdminPlaceContentDto>> byPlaceId =
                adminPlaceRepository.findContentsByPlaceIds(placeIds).stream()
                        .collect(Collectors.groupingBy(AdminPlaceContentDto::getPlaceId));

        places.forEach(place ->
                place.setContents(byPlaceId.getOrDefault(place.getPlaceId(), Collections.emptyList())));
    }

    private AdminPlaceDto getExistingPlace(Long placeId) {
        AdminPlaceDto place = adminPlaceRepository.findPlaceById(placeId);
        if (place == null) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        return place;
    }
}
