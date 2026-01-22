package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("meal_weekly_menu")
public class MealWeeklyMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    
    /** 状态: 0-草稿 1-已发布 */
    private Integer status;
    
    private LocalDateTime publishTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
