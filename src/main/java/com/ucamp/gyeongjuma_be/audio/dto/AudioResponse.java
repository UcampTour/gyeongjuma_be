package com.ucamp.gyeongjuma_be.audio.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AudioResponse {

    private Long audioId;
    private String audioUrl;
    private String imageUrl;
    private String title;
    private String script;
    private String playTime;
}
