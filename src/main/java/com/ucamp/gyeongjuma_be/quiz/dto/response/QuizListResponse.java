package com.ucamp.gyeongjuma_be.quiz.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuizListResponse {

    private List<visitedPlaceQuizDto> visitedPlaceQuizDtoList;
    private List<lockedPlaceQuizDto> lockedPlaceQuizDtoList;
}
