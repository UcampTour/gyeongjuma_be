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
    public int syncPlaceDetails() {
        List<Place> places = placeRepository.findAll();

        if (places.isEmpty()) {
            return 0;
        }

        int updateCount = 0;

        for (Place place : places) {
            Place detail = tourApiClient.getPlaceDetail(place.getPlaceId());

            if (detail == null) {
                continue;
            }

            updateCount += placeRepository.updateDetail(detail);
        }

        return updateCount;
    }

    @Override
    public int syncPlaceContents() {
        List<Place> places = placeRepository.findAll();

        if (places.isEmpty()) {
            return 0;
        }

        int saveCount = 0;

        for (Place place : places) {
            String overview = tourApiClient.getPlaceOverview(place.getPlaceId());

            if (overview == null) {
                continue;
            }

            placeRepository.deletePlaceContent(place.getPlaceId(), "KOR", "NORMAL");
            saveCount += placeRepository.savePlaceContent(place.getPlaceId(), overview, "KOR", "NORMAL");
        }

        return saveCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceSearchResponse> searchPlaces(Long memberId, String search, String sort, Double latitude, Double longitude) {

        return placeRepository.searchPlaces(search, sort, latitude, longitude, memberId);
    }

}
