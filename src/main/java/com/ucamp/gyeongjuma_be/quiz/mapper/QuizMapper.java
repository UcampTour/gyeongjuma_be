package com.ucamp.gyeongjuma_be.quiz.mapper;

import com.ucamp.gyeongjuma_be.quiz.domain.Quiz;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuizMapper {
    // 전체 퀴즘 목록 조회
    List<Quiz> findAll();
}
