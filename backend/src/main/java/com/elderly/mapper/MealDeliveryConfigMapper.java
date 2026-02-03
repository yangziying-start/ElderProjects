package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.MealDeliveryConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MealDeliveryConfigMapper extends BaseMapper<MealDeliveryConfig> {
    
    @Select("SELECT * FROM meal_delivery_config WHERE meal_type = #{mealType} AND status = 1 LIMIT 1")
    MealDeliveryConfig selectByMealType(@Param("mealType") Integer mealType);
}
