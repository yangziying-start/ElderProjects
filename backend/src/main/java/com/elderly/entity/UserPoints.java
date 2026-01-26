package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_points")
public class UserPoints {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    /** 当前积分 */
    private Integer points;
    
    /** 累计获得积分 */
    private Integer totalEarned;
    
    /** 累计消费积分 */
    private Integer totalSpent;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
