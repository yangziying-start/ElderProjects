package com.elderly.service.impl;

import com.elderly.entity.PointsTransaction;
import com.elderly.entity.UserPoints;
import com.elderly.mapper.PointsTransactionMapper;
import com.elderly.mapper.UserPointsMapper;
import com.elderly.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final UserPointsMapper userPointsMapper;
    private final PointsTransactionMapper transactionMapper;

    @Override
    public UserPoints getUserPoints(Long userId) {
        UserPoints points = userPointsMapper.selectByUserId(userId);
        if (points == null) {
            points = new UserPoints();
            points.setUserId(userId);
            points.setPoints(0);
            points.setTotalEarned(0);
            points.setTotalSpent(0);
            userPointsMapper.insert(points);
        }
        return points;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consume(Long userId, Integer amount, String orderType, Long orderId, String remark) {
        UserPoints points = getUserPoints(userId);
        if (points.getPoints() < amount) {
            throw new RuntimeException("积分不足");
        }
        points.setPoints(points.getPoints() - amount);
        points.setTotalSpent(points.getTotalSpent() + amount);
        userPointsMapper.updateById(points);
        
        recordTransaction(userId, 2, amount, points.getPoints(), orderType, orderId, remark);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refund(Long userId, Integer amount, String orderType, Long orderId, String remark) {
        UserPoints points = getUserPoints(userId);
        points.setPoints(points.getPoints() + amount);
        userPointsMapper.updateById(points);
        
        recordTransaction(userId, 3, amount, points.getPoints(), orderType, orderId, remark);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deduct(Long userId, Integer amount, String orderType, Long orderId, String remark) {
        UserPoints points = getUserPoints(userId);
        points.setPoints(Math.max(0, points.getPoints() - amount));
        userPointsMapper.updateById(points);
        
        recordTransaction(userId, 4, amount, points.getPoints(), orderType, orderId, remark);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recharge(Long userId, Integer amount, String remark) {
        UserPoints points = getUserPoints(userId);
        points.setPoints(points.getPoints() + amount);
        points.setTotalEarned(points.getTotalEarned() + amount);
        userPointsMapper.updateById(points);
        
        recordTransaction(userId, 1, amount, points.getPoints(), null, null, remark);
        return true;
    }

    private void recordTransaction(Long userId, Integer type, Integer amount, Integer balance,
                                   String orderType, Long orderId, String remark) {
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUserId(userId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalance(balance);
        transaction.setOrderType(orderType);
        transaction.setOrderId(orderId);
        transaction.setRemark(remark);
        transactionMapper.insert(transaction);
    }
}
