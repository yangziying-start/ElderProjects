package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("medical_doctor")
public class MedicalDoctor {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String name;
    private String title;
    private String specialty;
    private String introduction;
    private String avatar;
    
    /** 最大排队人数限制 */
    private Integer maxQueueLimit;
    
    /** 状态: 0-停诊 1-正常 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
    
    /** 当前排队人数(非数据库字段) */
    @TableField(exist = false)
    private Integer currentQueueCount;
    
    /** 是否熔断(非数据库字段) */
    @TableField(exist = false)
    private Boolean isCircuitOpen;
    
    /** 今日值班信息(非数据库字段) */
    @TableField(exist = false)
    private MedicalDoctorDuty todayDuty;
}
