package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("cleaning_order")
public class CleaningOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    private Long elderlyId;
    private Long bookerId;
    private Long workerId;
    private Long serviceId;
    private LocalDate serviceDate;
    private LocalTime startTime;
    private LocalTime endTime;
    
    /** 服务时长(分钟) */
    private Integer duration;
    
    /** 金额(积分) */
    private Integer amount;
    
    private String address;
    private String serviceCode;
    
    /** 状态: 0-待服务 1-服务中 2-待确认 3-已完成 4-已取消 */
    private Integer status;
    
    private LocalDateTime cancelTime;
    private Integer cancelDeduction;
    private Integer refundAmount;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String evidence;
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
    
    /** 保洁服务信息(非数据库字段) */
    @TableField(exist = false)
    private CleaningService service;
    
    /** 保洁员信息(非数据库字段) */
    @TableField(exist = false)
    private User worker;
}
