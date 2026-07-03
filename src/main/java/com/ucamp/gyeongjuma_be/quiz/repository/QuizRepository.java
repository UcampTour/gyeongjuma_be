package com.ucamp.gyeongjuma_be.quiz.repository;

import com.ucamp.gyeongjuma_be.quiz.dto.response.lockedPlaceQuizDto;
import com.ucamp.gyeongjuma_be.quiz.dto.response.visitedPlaceQuizDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface QuizRepository {

    // 방문한 장소 퀴즈 목록 조회
    List<visitedPlaceQuizDto> findVisitedPlaceQuizList(Long memberId);
    // 미방문 장소 퀴즈 목록 조회
    List<lockedPlaceQuizDto> findLockedPlaceQuizList(Long memberId);
}
