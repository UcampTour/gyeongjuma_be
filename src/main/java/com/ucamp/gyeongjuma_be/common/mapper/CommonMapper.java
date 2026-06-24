package com.ucamp.gyeongjuma_be.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommonMapper {

    @Select("SELECT NOW()")
    String checkDbTime();
}
