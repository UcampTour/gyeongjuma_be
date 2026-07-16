package com.ucamp.gyeongjuma_be.audio.domain;

import lombok.Builder;
import lombok.Data;

@Data
public class Audio {

    private final String audioUrl;
    private final Long placeId;
    private final String imageUrl;
    private final String title;
    private final String script;
    private final String playTime;

    @Builder
    public Audio(String audioUrl, Long placeId, String imageUrl, String title, String script, String playTime) {
        this.audioUrl = audioUrl;
        this.placeId = placeId;
        this.imageUrl = imageUrl;
        this.title = title;
        this.script = script;
        this.playTime = playTime;
    }
}
