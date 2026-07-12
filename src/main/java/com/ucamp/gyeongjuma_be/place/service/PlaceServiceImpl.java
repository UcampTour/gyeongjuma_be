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
    public List<PlaceSearchResponse> searchPlaces(Long memberId, String search, String sort, Double latitude, Double longitude) {

        return placeRepository.searchPlaces(search, sort, latitude, longitude, memberId);
    }

}
