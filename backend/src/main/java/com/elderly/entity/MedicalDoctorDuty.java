package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("medical_doctor_duty")
public class MedicalDoctorDuty {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long doctorId;
    private LocalDate dutyDate;
    
    /** 值班类型: 1-日间(8:00-20:00) 2-夜间急诊 */
    private Integer dutyType;
    
    private LocalTime startTime;
    private LocalTime endTime;
    
    /** 是否夜间值班: 0-否 1-是 */
    private Integer isNightShift;
    
    /** 状态: 0-休息 1-值班中 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
