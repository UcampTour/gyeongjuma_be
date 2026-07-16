package com.ucamp.gyeongjuma_be.audio.service;

import com.ucamp.gyeongjuma_be.audio.domain.Audio;
import com.ucamp.gyeongjuma_be.audio.dto.AudioResponse;
import com.ucamp.gyeongjuma_be.audio.repository.AudioRepository;
import com.ucamp.gyeongjuma_be.place.domain.Place;
import com.ucamp.gyeongjuma_be.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AudioServiceImpl implements AudioService {

    private final PlaceRepository placeRepository;
    private final AudioRepository audioRepository;
    private final AudioApiClient audioApiClient;

    @Override
    public int syncAudios() {
        int savedCount = 0;
        List<Place> places = placeRepository.findAll();

        for (Place place : places) {
            if (place.getPlaceName() == null || place.getPlaceName().isBlank()) {
                continue;
            }

            List<Audio> audios = audioApiClient.searchAudios(place.getPlaceId(), place.getPlaceName());
            for (Audio audio : audios) {
                if (!audioRepository.existsByPlaceIdAndAudioUrl(place.getPlaceId(), audio.getAudioUrl())) {
                    savedCount += audioRepository.save(audio);
                }
            }
        }

        return savedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudioResponse> getAudios(Long placeId) {
        return audioRepository.findByPlaceId(placeId);
    }
}
