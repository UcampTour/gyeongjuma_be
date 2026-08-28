package com.ucamp.gyeongjuma_be.visit.service;

import com.ucamp.gyeongjuma_be.visit.dto.VisitRequest;
import com.ucamp.gyeongjuma_be.visit.dto.VisitResponse;

import java.util.List;

public interface VisitService {
    List<Long> getRecentVisits(Long memberId);

    VisitResponse certifyVisit(Long memberId, Long placeId, VisitRequest request);
}
