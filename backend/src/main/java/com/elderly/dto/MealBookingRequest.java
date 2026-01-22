package com.elderly.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MealBookingRequest {
    /** 菜品ID */
    private Long dishId;
    
    /** 用餐日期 */
    private LocalDate dishDate;
    
    /** 餐次: 1-早餐 2-午餐 3-晚餐 */
    private Integer mealType;
    
    /** 数量 */
    private Integer quantity;
    
    /** 送餐地址 */
    private String address;
    
    /** 备注 */
    private String remark;
    
    /** 老人ID(子女代预约时使用) */
    private Long elderlyId;
    
    /** 服务人员ID */
    private Long workerId;
}
