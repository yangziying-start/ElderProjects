package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("service_time_slot")
public class ServiceTimeSlot {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long serviceItemId;
    
    /** 开始时间 */
    private LocalTime startTime;
    
    /** 结束时间 */
    private LocalTime endTime;
    
    /** 该时段容量 */
    private Integer capacity;
    
    /** 状态: 0-禁用 1-启用 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /** 剩余容量(非数据库字段) */
    @TableField(exist = false)
    private Integer remainingCapacity;
}
