package com.ucamp.gyeongjuma_be.audio.service;

import com.ucamp.gyeongjuma_be.audio.dto.AudioResponse;

import java.util.List;

public interface AudioService {

    int syncAudios();

    List<AudioResponse> getAudios(Long placeId);
}
