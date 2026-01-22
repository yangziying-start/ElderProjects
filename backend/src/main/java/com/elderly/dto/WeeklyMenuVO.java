package com.elderly.dto;

import com.elderly.entity.MealDailyDish;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class WeeklyMenuVO {
    /** 周开始日期 */
    private LocalDate weekStartDate;
    
    /** 周结束日期 */
    private LocalDate weekEndDate;
    
    /** 今日菜品 */
    private List<MealDailyDish> todayDishes;
    
    /** 周菜品(按日期字符串分组，格式: yyyy-MM-dd) */
    private Map<String, List<MealDailyDish>> weeklyDishes;
}
