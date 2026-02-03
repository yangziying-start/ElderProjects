package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("meal_delivery_config")
public class MealDeliveryConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 餐次: 1-早餐 2-午餐 3-晚餐 */
    private Integer mealType;
    
    private LocalTime deliveryStartTime;
    private LocalTime deliveryEndTime;
    
    /** 预约截止时间(送餐前X分钟) */
    private Integer bookingDeadlineMinutes;
    
    /** 状态: 0-禁用 1-启用 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
