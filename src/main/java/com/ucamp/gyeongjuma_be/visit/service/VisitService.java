package com.ucamp.gyeongjuma_be.visit.service;

import com.ucamp.gyeongjuma_be.visit.dto.VisitRequest;
import com.ucamp.gyeongjuma_be.visit.dto.VisitResponse;

public interface VisitService {
    VisitResponse certifyVisit(Long memberId, Long placeId, VisitRequest request);
}
