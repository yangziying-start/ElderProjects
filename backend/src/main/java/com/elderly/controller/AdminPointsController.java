package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.common.annotation.Log;
import com.elderly.entity.PointsTransaction;
import com.elderly.entity.User;
import com.elderly.entity.UserPoints;
import com.elderly.mapper.PointsTransactionMapper;
import com.elderly.mapper.UserMapper;
import com.elderly.mapper.UserPointsMapper;
import com.elderly.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台 - 积分管理
 */
@RestController
@RequestMapping("/api/admin/points")
@RequiredArgsConstructor
public class AdminPointsController {

    private final PointsService pointsService;
    private final UserPointsMapper userPointsMapper;
    private final PointsTransactionMapper transactionMapper;
    private final UserMapper userMapper;

    /**
     * 获取用户积分信息
     */
    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserPoints(@PathVariable Long userId) {
        UserPoints points = pointsService.getUserPoints(userId);
        User user = userMapper.selectById(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("userName", user != null ? user.getName() : "");
        result.put("phone", user != null ? user.getPhone() : "");
        result.put("points", points.getPoints());
        result.put("totalEarned", points.getTotalEarned());
        result.put("totalSpent", points.getTotalSpent());
        return Result.success(result);
    }

    /**
     * 为用户充值积分
     */
    @PostMapping("/recharge")
    @Log(module = "积分管理", operationType = 2, desc = "'积分充值, 用户ID: ' + #body['userId'] + ' 金额: ' + #body['amount']")
    public Result<Void> recharge(@RequestBody Map<String, Object> body) {
        Long userId = Long.parseLong(body.get("userId").toString());
        Integer amount = Integer.parseInt(body.get("amount").toString());
        String remark = (String) body.getOrDefault("remark", "管理员充值");
        
        if (amount <= 0) {
            return Result.error("充值金额必须大于0");
        }
        
        pointsService.recharge(userId, amount, remark);
        return Result.success();
    }

    /**
     * 扣除用户积分
     */
    @PostMapping("/deduct")
    @Log(module = "积分管理", operationType = 2, desc = "'积分扣除, 用户ID: ' + #body['userId'] + ' 金额: ' + #body['amount']")
    public Result<Void> deduct(@RequestBody Map<String, Object> body) {
        Long userId = Long.parseLong(body.get("userId").toString());
        Integer amount = Integer.parseInt(body.get("amount").toString());
        String remark = (String) body.getOrDefault("remark", "管理员扣除");
        
        if (amount <= 0) {
            return Result.error("扣除金额必须大于0");
        }
        
        UserPoints points = pointsService.getUserPoints(userId);
        if (points.getPoints() < amount) {
            return Result.error("用户积分不足");
        }
        
        pointsService.deduct(userId, amount, null, null, remark);
        return Result.success();
    }

    /**
     * 查询积分流水记录
     */
    @GetMapping("/transactions")
    public Result<Page<PointsTransaction>> pageTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer type) {
        
        Page<PointsTransaction> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PointsTransaction> wrapper = new LambdaQueryWrapper<PointsTransaction>()
                .eq(userId != null, PointsTransaction::getUserId, userId)
                .eq(type != null, PointsTransaction::getType, type)
                .orderByDesc(PointsTransaction::getCreateTime);
        
        return Result.success(transactionMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 获取用户积分流水
     */
    @GetMapping("/user/{userId}/transactions")
    public Result<Page<PointsTransaction>> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Page<PointsTransaction> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PointsTransaction> wrapper = new LambdaQueryWrapper<PointsTransaction>()
                .eq(PointsTransaction::getUserId, userId)
                .orderByDesc(PointsTransaction::getCreateTime);
        
        return Result.success(transactionMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 积分统计概览
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        // 总充值积分
        Long totalRecharge = transactionMapper.sumAmountByType(1);
        // 总消费积分
        Long totalConsume = transactionMapper.sumAmountByType(2);
        // 总退款积分
        Long totalRefund = transactionMapper.sumAmountByType(3);
        // 有积分的用户数
        Long userCount = userPointsMapper.countUsersWithPoints();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecharge", totalRecharge != null ? totalRecharge : 0);
        stats.put("totalConsume", totalConsume != null ? totalConsume : 0);
        stats.put("totalRefund", totalRefund != null ? totalRefund : 0);
        stats.put("userCount", userCount != null ? userCount : 0);
        return Result.success(stats);
    }
}
