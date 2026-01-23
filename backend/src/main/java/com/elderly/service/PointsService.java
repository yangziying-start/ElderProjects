package com.elderly.service;

import com.elderly.entity.UserPoints;

public interface PointsService {
    
    /**
     * 获取用户积分
     */
    UserPoints getUserPoints(Long userId);
    
    /**
     * 消费积分
     */
    boolean consume(Long userId, Integer amount, String orderType, Long orderId, String remark);
    
    /**
     * 退还积分
     */
    boolean refund(Long userId, Integer amount, String orderType, Long orderId, String remark);
    
    /**
     * 扣除积分(取消订单扣除)
     */
    boolean deduct(Long userId, Integer amount, String orderType, Long orderId, String remark);
    
    /**
     * 充值积分
     */
    boolean recharge(Long userId, Integer amount, String remark);
}
