package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("medical_circuit_breaker")
public class MedicalCircuitBreaker {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long doctorId;
    
    /** 当前排队人数 */
    private Integer currentQueueCount;
    
    /** 熔断是否开启: 0-关闭(可接单) 1-开启(停止接单) */
    private Integer isOpen;
    
    private LocalDateTime lastUpdateTime;
}
