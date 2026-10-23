package com.cuotiben.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 错题-标签关联表：联合主键，用注解方式手写简单 SQL。
 */
@Mapper
public interface MistakeTagMapper {

    @Insert("INSERT IGNORE INTO mistake_tag (mistake_id, tag_id) VALUES (#{mistakeId}, #{tagId})")
    int insertOne(@Param("mistakeId") Long mistakeId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM mistake_tag WHERE mistake_id = #{mistakeId}")
    int deleteByMistakeId(@Param("mistakeId") Long mistakeId);

    @Delete("DELETE FROM mistake_tag WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Long tagId);

    @Select("SELECT tag_id FROM mistake_tag WHERE mistake_id = #{mistakeId}")
    List<Long> selectTagIds(@Param("mistakeId") Long mistakeId);

    @Select("SELECT mistake_id FROM mistake_tag WHERE tag_id = #{tagId}")
    List<Long> selectMistakeIds(@Param("tagId") Long tagId);
}
