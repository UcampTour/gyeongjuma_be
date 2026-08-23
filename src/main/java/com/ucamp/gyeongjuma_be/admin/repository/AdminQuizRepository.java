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
                                       @Param("language") String language,
                                       @Param("isActive") Boolean isActive,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    long countQuizSets(@Param("keyword") String keyword,
                       @Param("placeId") Long placeId,
                       @Param("difficulty") String difficulty,
                       @Param("language") String language,
                       @Param("isActive") Boolean isActive);

    AdminQuizSetDto findQuizSetById(@Param("placeQuizInfoId") Long placeQuizInfoId);

    List<AdminQuizQuestionDto> findQuestionsBySetId(@Param("placeQuizInfoId") Long placeQuizInfoId);

    List<AdminQuizAnswerDto> findAnswersByQuizIds(@Param("quizIds") List<Long> quizIds);

    boolean existsActivePlace(@Param("placeId") Long placeId);

    int insertQuizSet(@Param("placeId") Long placeId,
                      @Param("title") String title,
                      @Param("description") String description,
                      @Param("difficulty") String difficulty,
                      @Param("language") String language,
                      @Param("originInfoId") Long originInfoId,
                      @Param("createdAt") LocalDateTime createdAt);

    int insertQuestion(@Param("question") String question,
                       @Param("originQuizId") Long originQuizId,
                       @Param("createdAt") LocalDateTime createdAt);

    int insertAnswer(@Param("quizId") Long quizId,
                     @Param("content") String content,
                     @Param("isCorrect") boolean isCorrect);

    int linkQuizToSet(@Param("placeQuizInfoId") Long placeQuizInfoId,
                      @Param("quizId") Long quizId);

    Long findLastInsertId();

    /** 원본 세트에 이미 해당 언어의 번역본이 있는지 */
    boolean existsTranslation(@Param("originInfoId") Long originInfoId,
                              @Param("language") String language);

    /** 원본 세트에 속한 활성 문항 ID 목록 — 번역 요청이 원본과 맞는지 검증하는 데 쓴다 */
    List<Long> findQuizIdsBySetId(@Param("placeQuizInfoId") Long placeQuizInfoId);

    /** 특정 세트의 번역본 목록 (원본 세트 ID로 조회) */
    List<AdminQuizSetDto> findTranslationsByOriginId(@Param("originInfoId") Long originInfoId);

    int softDeleteQuizSet(@Param("placeQuizInfoId") Long placeQuizInfoId,
                          @Param("deletedAt") LocalDateTime deletedAt);

    int softDeleteQuestionsBySetId(@Param("placeQuizInfoId") Long placeQuizInfoId,
                                   @Param("deletedAt") LocalDateTime deletedAt);
}
