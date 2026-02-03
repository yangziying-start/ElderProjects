package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜品模板/菜单库
 */
@Data
@TableName("meal_dish_template")
public class MealDishTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 菜品名称 */
    private String dishName;
    
    /** 菜品分类: 1-主食 2-荤菜 3-素菜 4-汤品 5-点心 */
    private Integer category;
    
    /** 适用餐次: 1-早餐 2-午餐 3-晚餐，多个用逗号分隔如"1,2,3" */
    private String mealTypes;
    
    /** 默认价格(积分) */
    private Integer price;
    
    /** 菜品图片 */
    private String image;
    
    /** 描述 */
    private String description;
    
    /** 营养信息 */
    private String nutritionInfo;
    
    /** 状态: 0-禁用 1-启用 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
