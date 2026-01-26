package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("cleaning_worker_schedule")
public class CleaningWorkerSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long workerId;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    
    /** 状态: 0-休息 1-可预约 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /** 保洁员信息(非数据库字段) */
    @TableField(exist = false)
    private User worker;
    
    /** 已预约时段(非数据库字段) */
    @TableField(exist = false)
    private java.util.List<CleaningOrder> bookedSlots;
}
