package com.ucamp.gyeongjuma_be.congestion.repository;

import com.ucamp.gyeongjuma_be.congestion.domain.Congestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CongestionRepository {

    void saveAll(@Param("congestions") List<Congestion> congestions);
}
