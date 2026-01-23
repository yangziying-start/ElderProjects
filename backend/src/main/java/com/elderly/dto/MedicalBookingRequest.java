package com.elderly.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MedicalBookingRequest {
    /** 医生ID */
    private Long doctorId;
    
    /** 预约日期 */
    private LocalDate appointmentDate;
    
    /** 预约类型: 1-日间巡诊 2-夜间急诊 */
    private Integer appointmentType;
    
    /** 症状描述 */
    private String symptoms;
    
    /** 巡诊地址 */
    private String address;
    
    /** 备注 */
    private String remark;
    
    /** 老人ID(子女代预约时使用) */
    private Long elderlyId;
}
