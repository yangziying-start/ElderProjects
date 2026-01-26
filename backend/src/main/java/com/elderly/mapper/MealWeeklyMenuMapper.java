package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.MealWeeklyMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;

@Mapper
public interface MealWeeklyMenuMapper extends BaseMapper<MealWeeklyMenu> {
    
    @Select("SELECT * FROM meal_weekly_menu WHERE week_start_date <= #{date} AND week_end_date >= #{date} AND status = 1 AND deleted = 0 LIMIT 1")
    MealWeeklyMenu selectByDate(@Param("date") LocalDate date);
}
