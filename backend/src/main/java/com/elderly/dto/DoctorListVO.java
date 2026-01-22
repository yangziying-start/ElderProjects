package com.elderly.dto;

import lombok.Data;

@Data
public class DoctorListVO {
    /** 医生ID */
    private Long doctorId;
    
    /** 医生姓名 */
    private String name;
    
    /** 职称 */
    private String title;
    
    /** 专长 */
    private String specialty;
    
    /** 头像 */
    private String avatar;
    
    /** 当前排队人数 */
    private Integer currentQueueCount;
    
    /** 最大排队限制 */
    private Integer maxQueueLimit;
    
    /** 是否可预约 */
    private Boolean available;
    
    /** 不可预约原因 */
    private String unavailableReason;
    
    /** 值班类型: 1-日间 2-夜间急诊 */
    private Integer dutyType;
    
    /** 值班开始时间 */
    private String startTime;
    
    /** 值班结束时间 */
    private String endTime;
    
    /** 熔断是否开启 */
    private Boolean isOpen;
    
    /** 是否可预约(综合判断) */
    private Boolean canBook;
    
    /** 是否夜间值班 */
    private Boolean isNightShift;
}
