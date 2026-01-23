package com.elderly.dto;

import lombok.Data;

@Data
public class TimeSlotVO {
    private Long id;
    private String startTime;
    private String endTime;
    private Integer capacity;
    private Integer bookedCount;
    private Integer remainingCapacity;
    
    /** 是否已约满 */
    private Boolean isFull;
    
    /** 显示文本，如 "08:00-10:00" */
    private String displayText;
}
