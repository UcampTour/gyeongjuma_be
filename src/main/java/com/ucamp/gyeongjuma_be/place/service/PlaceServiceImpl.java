package com.ucamp.gyeongjuma_be.place.service;

import com.ucamp.gyeongjuma_be.place.domain.Place;
import com.ucamp.gyeongjuma_be.place.dto.PlaceListResponse;
import com.ucamp.gyeongjuma_be.place.dto.PlaceSearchResponse;
import com.ucamp.gyeongjuma_be.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final TourApiClient tourApiClient;

    @Override
    public List<PlaceListResponse> syncPlaces() {
        List<Place> places = tourApiClient.getPlaceList();

        if (places.isEmpty()) {
            return List.of();
        }

        placeRepository.saveAll(places);

        return places.stream()
                .map(PlaceListResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceSearchResponse> searchPlaces(String search, String sort, Double latitude, Double longitude) {
        Long memberId = 1L;
        return placeRepository.searchPlaces(search, normalizeSort(sort), latitude, longitude, memberId);
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "distance";
        }

        return switch (sort.trim().toLowerCase()) {
            case "\uac70\ub9ac\uc21c", "distance", "dist" -> "distance";
            case "\uc608\uc0c1\ud63c\uc7a1\ub3c4", "\ud63c\uc7a1\ub3c4", "congestion", "congestion_score" -> "congestion";
            case "\ubbf8\ubc29\ubb38\uc9c0", "\ubbf8\ubc29\ubb38", "unvisited", "not_visited" -> "unvisited";
            default -> "distance";
        };
    }
}
