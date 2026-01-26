package com.elderly.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CleaningBookingRequest {
    /** 保洁员ID */
    private Long workerId;
    
    /** 保洁服务ID */
    private Long serviceId;
    
    /** 服务日期 */
    private LocalDate serviceDate;
    
    /** 开始时间 */
    private LocalTime startTime;
    
    /** 服务时长(分钟)，最小30分钟，30分钟为单位叠加 */
    private Integer duration;
    
    /** 服务地址 */
    private String address;
    
    /** 备注 */
    private String remark;
    
    /** 老人ID(子女代预约时使用) */
    private Long elderlyId;
}
