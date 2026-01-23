package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 送餐配送员值班安排
 */
@Data
@TableName("meal_worker_schedule")
public class MealWorkerSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 配送员ID */
    private Long workerId;
    
    /** 值班日期 */
    private LocalDate scheduleDate;
    
    /** 餐次: 1-早餐 2-午餐 3-晚餐, 为空表示全天 */
    private Integer mealType;
    
    /** 负责楼栋(逗号分隔)，为空表示全部楼栋 */
    private String buildings;
    
    /** 状态: 0-停用 1-启用 */
    private Integer status;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
    
    /** 配送员名称(非数据库字段) */
    @TableField(exist = false)
    private String workerName;
    
    /** 配送员电话(非数据库字段) */
    @TableField(exist = false)
    private String workerPhone;
}
