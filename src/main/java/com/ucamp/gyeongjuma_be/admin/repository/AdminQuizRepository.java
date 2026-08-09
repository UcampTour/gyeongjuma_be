package com.ucamp.gyeongjuma_be.admin.repository;

import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizAnswerDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizQuestionDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminQuizSetDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminQuizRepository {

    List<AdminQuizSetDto> findQuizSets(@Param("keyword") String keyword,
                                       @Param("placeId") Long placeId,
                                       @Param("difficulty") String difficulty,
                                       @Param("isActive") Boolean isActive,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    long countQuizSets(@Param("keyword") String keyword,
                       @Param("placeId") Long placeId,
                       @Param("difficulty") String difficulty,
                       @Param("isActive") Boolean isActive);

    AdminQuizSetDto findQuizSetById(@Param("placeQuizInfoId") Long placeQuizInfoId);

    List<AdminQuizQuestionDto> findQuestionsBySetId(@Param("placeQuizInfoId") Long placeQuizInfoId);

    List<AdminQuizAnswerDto> findAnswersByQuizIds(@Param("quizIds") List<Long> quizIds);

    boolean existsActivePlace(@Param("placeId") Long placeId);

    int insertQuizSet(@Param("placeId") Long placeId,
                      @Param("title") String title,
                      @Param("description") String description,
                      @Param("difficulty") String difficulty,
                      @Param("createdAt") LocalDateTime createdAt);

    int insertQuestion(@Param("question") String question,
                       @Param("createdAt") LocalDateTime createdAt);

    int insertAnswer(@Param("quizId") Long quizId,
                     @Param("content") String content,
                     @Param("isCorrect") boolean isCorrect);

    int linkQuizToSet(@Param("placeQuizInfoId") Long placeQuizInfoId,
                      @Param("quizId") Long quizId);

    Long findLastInsertId();

    int softDeleteQuizSet(@Param("placeQuizInfoId") Long placeQuizInfoId,
                          @Param("deletedAt") LocalDateTime deletedAt);

    int softDeleteQuestionsBySetId(@Param("placeQuizInfoId") Long placeQuizInfoId,
                                   @Param("deletedAt") LocalDateTime deletedAt);
}
