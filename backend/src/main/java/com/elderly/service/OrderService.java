package com.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderly.dto.BookingRequest;
import com.elderly.entity.Order;

public interface OrderService extends IService<Order> {
    /**
     * 创建预约订单（带容量检查）
     */
    Long createOrder(BookingRequest request, Long userId);
    
    /**
     * 旧版创建订单方法（兼容）
     */
    Long createOrder(Order order);
    
    boolean cancelOrder(Long orderId, Long userId);
    boolean acceptOrder(Long orderId, Long workerId);
    boolean startService(Long orderId, String serviceCode);
    boolean completeService(Long orderId, String evidence);
}
