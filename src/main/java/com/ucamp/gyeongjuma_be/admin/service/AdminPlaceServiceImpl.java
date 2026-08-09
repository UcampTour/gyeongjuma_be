package com.ucamp.gyeongjuma_be.admin.service;

import com.ucamp.gyeongjuma_be.admin.dto.request.PlaceCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceListResponse;
import com.ucamp.gyeongjuma_be.admin.repository.AdminPlaceRepository;
import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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

    private AdminPlaceDto getExistingPlace(Long placeId) {
        AdminPlaceDto place = adminPlaceRepository.findPlaceById(placeId);
        if (place == null) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        return place;
    }
}
