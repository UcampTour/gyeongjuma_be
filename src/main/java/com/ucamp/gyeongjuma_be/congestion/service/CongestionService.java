package com.ucamp.gyeongjuma_be.congestion.service;

import com.ucamp.gyeongjuma_be.congestion.dto.CongestionSyncResponse;

import java.util.List;

public interface CongestionService {

    List<CongestionSyncResponse> syncCongestions();
}
