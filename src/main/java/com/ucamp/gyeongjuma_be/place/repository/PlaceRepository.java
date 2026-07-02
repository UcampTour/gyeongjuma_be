package com.ucamp.gyeongjuma_be.place.repository;

import com.ucamp.gyeongjuma_be.place.domain.Place;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaceRepository {

    void saveAll(@Param("places") List<Place> places);

    List<Place> findAll();
}
