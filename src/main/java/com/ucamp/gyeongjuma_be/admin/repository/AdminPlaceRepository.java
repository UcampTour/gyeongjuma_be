package com.ucamp.gyeongjuma_be.admin.repository;

import com.ucamp.gyeongjuma_be.admin.dto.request.PlaceCreateRequest;
import com.ucamp.gyeongjuma_be.admin.dto.response.AdminPlaceDto;
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

    int restorePlace(@Param("placeId") Long placeId,
                     @Param("updatedAt") LocalDateTime updatedAt);
}
