package com.ucamp.gyeongjuma_be.quiz.repository;

import com.ucamp.gyeongjuma_be.quiz.domain.Quiz;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface QuizRepository {
    // 전체 퀴즘 목록 조회
    List<Quiz> findAll();
}
