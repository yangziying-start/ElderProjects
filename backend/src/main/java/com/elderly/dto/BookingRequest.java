package com.elderly.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingRequest {
    private Long serviceItemId;
    private Long timeSlotId;
    private LocalDate appointmentDate;
    private String address;
    private String remark;
    
    /** 老人ID(子女代预约时使用) */
    private Long elderlyId;
    
    /** 医疗预约健康信息 */
    private HealthInfo healthInfo;
    
    @Data
    public static class HealthInfo {
        /** 症状描述 */
        private String symptoms;
        /** 过敏史 */
        private String allergies;
        /** 常服药品 */
        private String medications;
    }
}
