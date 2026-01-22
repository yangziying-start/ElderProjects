package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {
    
    @Select("SELECT * FROM user_points WHERE user_id = #{userId} LIMIT 1")
    UserPoints selectByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM user_points WHERE points > 0")
    Long countUsersWithPoints();
}
