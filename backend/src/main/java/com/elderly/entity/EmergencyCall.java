package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("emergency_call")
public class EmergencyCall {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long elderlyId;
    
    /** 老人姓名 */
    private String elderlyName;
    
    /** 联系电话 */
    private String phone;
    
    private String address;
    
    /** 事件类型: 1-紧急医疗 2-摔倒 3-一般求助 */
    private Integer eventType;
    
    private LocalDateTime triggerTime;
    private Long responderId;
    private LocalDateTime responseTime;
    
    /** 是否升级到社区: 0-否 1-是 */
    private Integer escalated;
    
    /** 状态: 0-待处理 1-已处理 */
    private Integer status;
    
    private String result;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
