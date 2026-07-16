package com.ucamp.gyeongjuma_be.audio.repository;

import com.ucamp.gyeongjuma_be.audio.domain.Audio;
import com.ucamp.gyeongjuma_be.audio.dto.AudioResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AudioRepository {

    boolean existsByPlaceIdAndAudioUrl(@Param("placeId") Long placeId, @Param("audioUrl") String audioUrl);

    int save(Audio audio);

    List<AudioResponse> findByPlaceId(@Param("placeId") Long placeId);
}
