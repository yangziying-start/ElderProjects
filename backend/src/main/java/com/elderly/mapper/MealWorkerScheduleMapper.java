package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.MealWorkerSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MealWorkerScheduleMapper extends BaseMapper<MealWorkerSchedule> {
    
    /**
     * 获取指定日期和餐次的值班配送员
     */
    @Select("SELECT * FROM meal_worker_schedule WHERE schedule_date = #{date} " +
            "AND (meal_type IS NULL OR meal_type = #{mealType}) " +
            "AND status = 1 AND deleted = 0 " +
            "ORDER BY meal_type DESC")
    List<MealWorkerSchedule> selectByDateAndMealType(@Param("date") LocalDate date, @Param("mealType") Integer mealType);
    
    /**
     * 获取指定日期范围的值班安排
     */
    @Select("SELECT * FROM meal_worker_schedule WHERE schedule_date >= #{startDate} " +
            "AND schedule_date <= #{endDate} AND deleted = 0 " +
            "ORDER BY schedule_date, meal_type")
    List<MealWorkerSchedule> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
