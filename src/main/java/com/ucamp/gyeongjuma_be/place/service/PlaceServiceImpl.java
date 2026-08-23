package com.ucamp.gyeongjuma_be.place.service;

import com.ucamp.gyeongjuma_be.place.domain.Place;
import com.ucamp.gyeongjuma_be.place.dto.PlaceListResponse;
import com.ucamp.gyeongjuma_be.place.dto.PlaceSearchResponse;
import com.ucamp.gyeongjuma_be.place.repository.PlaceRepository;
import com.ucamp.gyeongjuma_be.place.service.TourApiClient.PlaceOverview;
import com.ucamp.gyeongjuma_be.place.service.TourApiClient.TourApiLocale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final TourApiClient tourApiClient;
    private final TransactionTemplate transactionTemplate;

    @Override
    public List<PlaceListResponse> syncPlaces() {
        List<Place> places = allLocales().stream()
                .flatMap(locale -> tourApiClient.getPlaceList(locale).stream())
                .toList();

        if (places.isEmpty()) {
            return List.of();
        }

        transactionTemplate.executeWithoutResult(status -> placeRepository.saveAll(places));

        return places.stream()
                .map(PlaceListResponse::from)
                .toList();
    }

    @Override
    public int syncPlaceDetails() {
        int updateCount = 0;

        for (TourApiLocale locale : allLocales()) {
            List<Place> places = placeRepository.findAllByLanguage(locale.contentLanguage());

            if (places.isEmpty()) {
                continue;
            }

            for (Place place : places) {
                Place detail = tourApiClient.getPlaceDetail(place.getApiPlaceId(), locale);

                if (detail == null || !place.getApiPlaceId().equals(detail.getApiPlaceId())) {
                    continue;
                }

                Integer updated = transactionTemplate.execute(status -> placeRepository.updateDetail(detail));
                updateCount += updated == null ? 0 : updated;
            }
        }

        return updateCount;
    }

    @Override
    public int syncPlaceContents() {
        return syncPlaceContentsByLocale(TourApiLocale.KOREAN);
    }

    @Override
    public Map<String, Integer> syncPlaceContentsAllLanguages() {
        return allLocales().stream()
                .collect(java.util.stream.Collectors.toMap(
                        TourApiLocale::contentLanguage,
                        this::syncPlaceContentsByLocale,
                        (left, right) -> left,
                        java.util.LinkedHashMap::new
                ));
    }

    private int syncPlaceContentsByLocale(TourApiLocale locale) {
        List<Place> places = placeRepository.findAllByLanguage(locale.contentLanguage());

        if (places.isEmpty()) {
            return 0;
        }

        int saveCount = 0;

        for (Place place : places) {
            PlaceOverview placeOverview = tourApiClient.getPlaceOverview(place.getApiPlaceId(), locale);

            if (placeOverview == null || !place.getApiPlaceId().equals(placeOverview.contentId())) {
                continue;
            }

            Integer saved = transactionTemplate.execute(status -> {
                placeRepository.deletePlaceContent(place.getPlaceId(), locale.contentLanguage(), "NORMAL");
                return placeRepository.savePlaceContent(
                        place.getPlaceId(),
                        placeOverview.overview(),
                        locale.contentLanguage(),
                        "NORMAL"
                );
            });
            saveCount += saved == null ? 0 : saved;
        }

        return saveCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceSearchResponse> searchPlaces(Long memberId, String search, String sort, Double latitude, Double longitude) {

        return placeRepository.searchPlaces(search, sort, latitude, longitude, memberId);
    }

    private List<TourApiLocale> allLocales() {
        return List.of(
                TourApiLocale.KOREAN,
                TourApiLocale.ENGLISH,
                TourApiLocale.JAPANESE,
                TourApiLocale.CHINESE_SIMPLIFIED,
                TourApiLocale.CHINESE_TRADITIONAL,
                TourApiLocale.GERMAN,
                TourApiLocale.FRENCH,
                TourApiLocale.SPANISH,
                TourApiLocale.RUSSIAN
        );
    }

}
