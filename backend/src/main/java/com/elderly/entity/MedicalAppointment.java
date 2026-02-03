package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("medical_appointment")
public class MedicalAppointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    private Long elderlyId;
    private Long bookerId;
    private Long doctorId;
    private LocalDate appointmentDate;
    
    /** 预约类型: 1-日间巡诊 2-夜间急诊 */
    private Integer appointmentType;
    
    private Integer queueNumber;
    private String symptoms;
    private String address;
    
    /** 状态: 0-排队中 1-巡诊中 2-已完成 3-已取消 */
    private Integer status;
    
    private LocalDateTime cancelTime;
    private LocalDateTime visitTime;
    private String diagnosis;
    private String prescription;
    private String serviceCode;
    private String remark;
    
    /** 是否迟到: 0-否 1-是 */
    private Integer isLate;
    
    /** 迟到分钟数 */
    private Integer lateMinutes;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
    
    /** 医生信息(非数据库字段) */
    @TableField(exist = false)
    private MedicalDoctor doctor;
}
