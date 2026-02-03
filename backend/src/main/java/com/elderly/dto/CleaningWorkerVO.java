package com.elderly.dto;

import lombok.Data;
import java.time.LocalTime;
import java.util.List;

@Data
public class CleaningWorkerVO {
    /** 保洁员ID */
    private Long workerId;
    
    /** 保洁员姓名 */
    private String workerName;
    
    /** 头像 */
    private String avatar;
    
    /** 排班开始时间 */
    private LocalTime scheduleStartTime;
    
    /** 排班结束时间 */
    private LocalTime scheduleEndTime;
    
    /** 已预约时段 */
    private List<TimeSlot> bookedSlots;
    
    /** 可预约时段 */
    private List<TimeSlot> availableSlots;
    
    /** 是否已下班（当天值班时间已过） */
    private Boolean offDuty;
    
    /** 是否有排班 */
    private Boolean hasSchedule;
    
    @Data
    public static class TimeSlot {
        private LocalTime startTime;
        private LocalTime endTime;
        
        public TimeSlot() {}
        
        public TimeSlot(LocalTime startTime, LocalTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
