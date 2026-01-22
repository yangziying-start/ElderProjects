package com.elderly.service;

import com.elderly.dto.MealBookingRequest;
import com.elderly.dto.WeeklyMenuVO;
import com.elderly.entity.MealOrder;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MealService {
    
    /**
     * 获取周食谱(包含今日菜品)
     */
    WeeklyMenuVO getWeeklyMenu(LocalDate date);
    
    /**
     * 检查是否在预约窗口内
     */
    Map<String, Object> checkBookingWindow(LocalDate date, Integer mealType);
    
    /**
     * 创建餐饮预约订单
     */
    Long createOrder(MealBookingRequest request, Long userId);
    
    /**
     * 取消餐饮订单(带退款规则)
     */
    Map<String, Object> cancelOrder(Long orderId, Long userId);
    
    /**
     * 获取用户餐饮订单列表
     */
    List<MealOrder> getUserOrders(Long userId, Integer status);
}
