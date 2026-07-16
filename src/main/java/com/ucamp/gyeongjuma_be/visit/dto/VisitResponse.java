package com.ucamp.gyeongjuma_be.visit.dto;

public record VisitResponse(Long visitId, Long placeId, double distanceMeters, double radiusMeters) {
}
