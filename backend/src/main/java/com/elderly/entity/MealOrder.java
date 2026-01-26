package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("meal_order")
public class MealOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    private Long elderlyId;
    private Long bookerId;
    private Long dishId;
    private LocalDate dishDate;
    
    /** 餐次: 1-早餐 2-午餐 3-晚餐 */
    private Integer mealType;
    
    private Integer quantity;
    
    /** 金额(积分) */
    private Integer amount;
    
    private String address;
    private Long workerId;
    
    /** 服务码 */
    private String serviceCode;
    
    /** 状态: 0-待配送 1-配送中 2-已送达 3-已取消 */
    private Integer status;
    
    private LocalDateTime cancelTime;
    
    /** 取消扣除积分 */
    private Integer cancelDeduction;
    
    /** 退还积分 */
    private Integer refundAmount;
    
    private String remark;
    
    /** 是否迟到: 0-否 1-是 */
    private Integer isLate;
    
    /** 迟到分钟数 */
    private Integer lateMinutes;
    
    /** 实际送达时间 */
    private LocalDateTime actualDeliveryTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
    
    /** 菜品信息(非数据库字段) */
    @TableField(exist = false)
    private MealDailyDish dish;
    
    /** 配送员名称(非数据库字段) */
    @TableField(exist = false)
    private String workerName;
    
    /** 配送员电话(非数据库字段) */
    @TableField(exist = false)
    private String workerPhone;
}
