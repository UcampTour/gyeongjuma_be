package com.ucamp.gyeongjuma_be.admin.repository;

import com.ucamp.gyeongjuma_be.admin.dto.request.PlaceCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceContentDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceDto;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminPlaceRepository {

    List<AdminPlaceDto> findPlaces(@Param("keyword") String keyword,
                                   @Param("isActive") Boolean isActive,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    long countPlaces(@Param("keyword") String keyword,
                     @Param("isActive") Boolean isActive);

    AdminPlaceDto findPlaceById(@Param("placeId") Long placeId);

    int insertPlace(@Param("req") PlaceCreateRequest request,
                    @Param("createdAt") LocalDateTime createdAt);

    Long findLastInsertedPlaceId();

    int softDeletePlace(@Param("placeId") Long placeId,
                        @Param("deletedAt") LocalDateTime deletedAt);

    /** 관광지 관리 화면용 목록 (해설 제외) */
    List<AdminPlaceItemDto> findPlaceItems(@Param("keyword") String keyword,
                                           @Param("isActive") Boolean isActive,
                                           @Param("offset") int offset,
                                           @Param("size") int size);

    AdminPlaceItemDto findPlaceItemById(@Param("placeId") Long placeId);

    /** 여러 관광지의 해설을 한 번에 조회해 화면에서 묶는다 (N+1 회피) */
    List<AdminPlaceContentDto> findContentsByPlaceIds(@Param("placeIds") List<Long> placeIds);

    /** (place_id, language, difficulty)가 같은 해설이 있으면 내용만 교체 */
    int upsertPlaceContent(@Param("placeId") Long placeId,
                           @Param("language") String language,
                           @Param("difficulty") String difficulty,
                           @Param("description") String description);

    int insertPlaceContent(@Param("placeId") Long placeId,
                           @Param("language") String language,
                           @Param("difficulty") String difficulty,
                           @Param("description") String description);

    int deletePlaceContent(@Param("placeContentId") Long placeContentId);

    AdminPlaceContentDto findContentById(@Param("placeContentId") Long placeContentId);

    int restorePlace(@Param("placeId") Long placeId,
                     @Param("updatedAt") LocalDateTime updatedAt);
}
