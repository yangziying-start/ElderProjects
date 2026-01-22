package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("points_transaction")
public class PointsTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    /** 类型: 1-充值 2-消费 3-退款 4-扣除 */
    private Integer type;
    
    /** 积分数量 */
    private Integer amount;
    
    /** 变动后余额 */
    private Integer balance;
    
    /** 关联订单类型: meal/cleaning/medical */
    private String orderType;
    
    private Long orderId;
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
