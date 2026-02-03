package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.Family;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FamilyMapper extends BaseMapper<Family> {
    
    /**
     * 根据用户ID获取其家庭信息
     */
    @Select("SELECT f.* FROM family f INNER JOIN sys_user u ON u.family_id = f.id WHERE u.id = #{userId} AND f.deleted = 0")
    Family selectByUserId(@Param("userId") Long userId);
}
