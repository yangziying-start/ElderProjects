package com.elderly.service;

import com.elderly.dto.CleaningBookingRequest;
import com.elderly.dto.CleaningWorkerVO;
import com.elderly.entity.CleaningOrder;
import com.elderly.entity.CleaningService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CleaningBookingService {
    
    /**
     * 获取保洁服务项目列表
     */
    List<CleaningService> getServiceList();
    
    /**
     * 获取指定日期可预约的保洁员列表(含排班和已预约时段)
     */
    List<CleaningWorkerVO> getAvailableWorkers(LocalDate date);
    
    /**
     * 创建保洁预约订单
     */
    Long createOrder(CleaningBookingRequest request, Long userId);
    
    /**
     * 取消保洁订单(带退款规则)
     */
    Map<String, Object> cancelOrder(Long orderId, Long userId);
    
    /**
     * 获取用户保洁订单列表
     */
    List<CleaningOrder> getUserOrders(Long userId, Integer status);
    
    /**
     * 开始服务(验证服务码)
     */
    boolean startService(Long orderId, String serviceCode);
    
    /**
     * 完成服务
     */
    boolean completeService(Long orderId, String evidence);
}
