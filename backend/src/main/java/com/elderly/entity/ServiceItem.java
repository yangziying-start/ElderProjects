package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_item")
public class ServiceItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private Long categoryId;
    private String description;
    private BigDecimal price;
    
    /** 服务时长(分钟) */
    private Integer duration;
    
    /** 是否需要服务码: 0-否 1-是 */
    private Integer needServiceCode;
    
    /** 每日容量 */
    private Integer dailyCapacity;
    
    /** 状态: 0-下架 1-上架 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
