package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("meal_daily_dish")
public class MealDailyDish {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long weeklyMenuId;
    private LocalDate dishDate;
    
    /** 餐次: 1-早餐 2-午餐 3-晚餐 */
    private Integer mealType;
    
    private String dishName;
    private String description;
    private String image;
    
    /** 价格(积分) */
    private Integer price;
    
    private String nutritionInfo;
    private Integer sort;
    
    /** 状态: 0-下架 1-上架 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
