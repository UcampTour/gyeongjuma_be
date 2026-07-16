package com.ucamp.gyeongjuma_be.visit.service;

import com.ucamp.gyeongjuma_be.common.exception.CustomException;
import com.ucamp.gyeongjuma_be.common.exception.ErrorCode;
import com.ucamp.gyeongjuma_be.place.domain.Place;
import com.ucamp.gyeongjuma_be.place.repository.PlaceRepository;
import com.ucamp.gyeongjuma_be.visit.domain.Visit;
import com.ucamp.gyeongjuma_be.visit.dto.VisitRequest;
import com.ucamp.gyeongjuma_be.visit.dto.VisitResponse;
import com.ucamp.gyeongjuma_be.visit.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {
    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private final PlaceRepository placeRepository;
    private final VisitRepository visitRepository;

    @Override
    @Transactional
    public VisitResponse certifyVisit(Long memberId, Long placeId, VisitRequest request) {
        validateCoordinates(request);
        Place place = placeRepository.findById(placeId);
        if (place == null || place.getMapY() == null || place.getMapX() == null) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        if (visitRepository.existsByMemberIdAndPlaceId(memberId, placeId)) {
            throw new CustomException(ErrorCode.ALREADY_VISITED);
        }

        double radiusMeters = parseRadius(place.getRadiusMeters());
        double distanceMeters = calculateDistanceMeters(request.latitude(), request.longitude(), place.getMapY(), place.getMapX());
        if (distanceMeters > radiusMeters) {
            throw new CustomException(ErrorCode.OUT_OF_VISIT_RADIUS);
        }

        Visit visit = new Visit(memberId, placeId);
        visitRepository.save(visit);
        return new VisitResponse(visit.getVisitId(), placeId, distanceMeters, radiusMeters);
    }

    private void validateCoordinates(VisitRequest request) {
        if (request == null || request.latitude() == null || request.longitude() == null
                || !Double.isFinite(request.latitude()) || !Double.isFinite(request.longitude())
                || request.latitude() < -90 || request.latitude() > 90
                || request.longitude() < -180 || request.longitude() > 180) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private double parseRadius(String radiusMeters) {
        try {
            double radius = Double.parseDouble(radiusMeters);
            if (!Double.isFinite(radius) || radius < 0) throw new NumberFormatException();
            return radius;
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private double calculateDistanceMeters(double latitude1, double longitude1, double latitude2, double longitude2) {
        double latitudeDelta = Math.toRadians(latitude2 - latitude1);
        double longitudeDelta = Math.toRadians(longitude2 - longitude1);
        double sinLatitude = Math.sin(latitudeDelta / 2);
        double sinLongitude = Math.sin(longitudeDelta / 2);
        double a = sinLatitude * sinLatitude + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) * sinLongitude * sinLongitude;
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
