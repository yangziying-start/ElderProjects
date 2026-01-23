package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cleaning_service")
public class CleaningService {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    
    /** 参考耗时(分钟) */
    private Integer referenceDuration;
    
    /** 每30分钟价格(积分) */
    @TableField("price_per_30min")
    private Integer pricePer30min;
    
    private String icon;
    private Integer sort;
    
    /** 状态: 0-下架 1-上架 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
