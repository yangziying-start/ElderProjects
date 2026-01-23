package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("service_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    private Long elderlyId;
    private Long serviceItemId;
    private Long workerId;
    
    /** 预约人ID(可能是子女代预约) */
    private Long bookerId;
    
    private LocalDateTime appointmentTime;
    
    /** 预约日期 */
    private LocalDate appointmentDate;
    
    /** 时间段ID */
    private Long timeSlotId;
    
    private String address;
    private BigDecimal amount;
    
    /** 服务码（6位数字，每日动态更新） */
    private String serviceCode;
    
    /** 状态: 0-待接单 1-已接单 2-服务中 3-待确认 4-已完成 5-已取消 */
    private Integer status;
    
    private String remark;
    
    /** 服务完成凭证（图片URL） */
    private String evidence;
    
    /** 服务开始时间 */
    private LocalDateTime serviceStartTime;
    
    /** 服务完成时间 */
    private LocalDateTime serviceEndTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
