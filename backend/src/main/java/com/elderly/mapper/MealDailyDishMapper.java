package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.MealDailyDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MealDailyDishMapper extends BaseMapper<MealDailyDish> {
    
    @Select("SELECT * FROM meal_daily_dish WHERE dish_date = #{date} AND status = 1 AND deleted = 0 ORDER BY meal_type, sort")
    List<MealDailyDish> selectByDate(@Param("date") LocalDate date);
    
    @Select("SELECT * FROM meal_daily_dish WHERE dish_date BETWEEN #{startDate} AND #{endDate} AND status = 1 AND deleted = 0 ORDER BY dish_date, meal_type, sort")
    List<MealDailyDish> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
