package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("health_record")
public class HealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long elderlyId;
    private Long orderId;
    
    /** 症状描述 */
    private String symptoms;
    
    /** 过敏史 */
    private String allergies;
    
    /** 常服药品 */
    private String medications;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
