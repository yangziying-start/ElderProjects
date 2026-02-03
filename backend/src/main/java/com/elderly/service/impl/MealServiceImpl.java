package com.elderly.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.dto.MealBookingRequest;
import com.elderly.dto.WeeklyMenuVO;
import com.elderly.entity.MealDailyDish;
import com.elderly.entity.MealDeliveryConfig;
import com.elderly.entity.MealOrder;
import com.elderly.entity.Family;
import com.elderly.mapper.*;
import com.elderly.service.MealService;
import com.elderly.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealDailyDishMapper dailyDishMapper;
    private final MealDeliveryConfigMapper deliveryConfigMapper;
    private final MealOrderMapper mealOrderMapper;
    private final UserMapper userMapper;
    private final PointsService pointsService;
    private final FamilyMapper familyMapper;

    @Override
    // key 生成策略：根据传入的 date 缓存，比如 "weekly_menu::2026-02-01"
    // 这样用户查不同日期的食谱，会存不同的缓存
    @Cacheable(value = "weekly_menu", key = "#date.toString()")
    public WeeklyMenuVO getWeeklyMenu(LocalDate date) {
        WeeklyMenuVO vo = new WeeklyMenuVO();
        
        // 获取传入日期所在周的周一（而不是当前日期的周一）
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        vo.setWeekStartDate(weekStart);
        vo.setWeekEndDate(weekEnd);
        
        // 获取传入日期的菜品（而不是今日）
        vo.setTodayDishes(dailyDishMapper.selectByDate(date));
        
        // 获取该周菜品（按日期字符串分组，确保前端能正确匹配）
        List<MealDailyDish> weekDishes = dailyDishMapper.selectByDateRange(weekStart, weekEnd);
        vo.setWeeklyDishes(weekDishes.stream().collect(
            Collectors.groupingBy(dish -> dish.getDishDate().toString())
        ));
        
        return vo;
    }

    @Override
    public Map<String, Object> checkBookingWindow(LocalDate date, Integer mealType) {
        Map<String, Object> result = new HashMap<>();
        MealDeliveryConfig config = deliveryConfigMapper.selectByMealType(mealType);
        
        if (config == null) {
            result.put("canBook", false);
            result.put("reason", "该餐次暂不支持预约");
            return result;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deliveryStart = date.atTime(config.getDeliveryStartTime());
        LocalDateTime bookingDeadline = deliveryStart.minusMinutes(config.getBookingDeadlineMinutes());
        
        // 计算上一餐送餐结束时间+1.5小时
        LocalDateTime bookingWindowStart = calculateBookingWindowStart(date, mealType);
        
        if (now.isBefore(bookingWindowStart)) {
            result.put("canBook", false);
            result.put("reason", "预约窗口尚未开启，请在" + bookingWindowStart.toLocalTime() + "后预约");
        } else if (now.isAfter(bookingDeadline)) {
            result.put("canBook", false);
            result.put("reason", "已过预约截止时间");
        } else {
            result.put("canBook", true);
            result.put("deadline", bookingDeadline);
        }
        
        return result;
    }

    private LocalDateTime calculateBookingWindowStart(LocalDate date, Integer mealType) {
        // 上一餐送餐结束1.5小时后开始预约
        int prevMealType = mealType == 1 ? 3 : mealType - 1;
        LocalDate prevDate = mealType == 1 ? date.minusDays(1) : date;
        
        MealDeliveryConfig prevConfig = deliveryConfigMapper.selectByMealType(prevMealType);
        if (prevConfig != null) {
            return prevDate.atTime(prevConfig.getDeliveryEndTime()).plusMinutes(90);
        }
        // 默认返回当天0点
        return date.atStartOfDay();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(MealBookingRequest request, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查预约窗口
        Map<String, Object> windowCheck = checkBookingWindow(request.getDishDate(), request.getMealType());
        if (!(Boolean) windowCheck.get("canBook")) {
            throw new RuntimeException((String) windowCheck.get("reason"));
        }
        
        // 获取菜品信息
        MealDailyDish dish = dailyDishMapper.selectById(request.getDishId());
        if (dish == null || dish.getStatus() != 1) {
            throw new RuntimeException("菜品不存在或已下架");
        }
        
        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        int totalAmount = dish.getPrice() * quantity;
        
        Long elderlyId = request.getElderlyId() != null ? request.getElderlyId() : userId;
        
        // 检查是否已有同一用户同一天同一餐次的待配送订单，如果有则合并
        MealOrder existingOrder = mealOrderMapper.selectOne(
            new LambdaQueryWrapper<MealOrder>()
                .eq(MealOrder::getElderlyId, elderlyId)
                .eq(MealOrder::getDishDate, request.getDishDate())
                .eq(MealOrder::getMealType, request.getMealType())
                .eq(MealOrder::getStatus, 0) // 待配送状态
                .eq(MealOrder::getDeleted, 0)
        );
        
        if (existingOrder != null) {
            // 合并订单：扣除积分并更新现有订单
            pointsService.consume(elderlyId, totalAmount, "meal", existingOrder.getId(), "餐饮加点-" + dish.getDishName());
            
            // 更新订单金额和数量
            existingOrder.setQuantity(existingOrder.getQuantity() + quantity);
            existingOrder.setAmount(existingOrder.getAmount() + totalAmount);
            
            // 如果是不同菜品，追加到备注中
            String dishInfo = dish.getDishName() + " x" + quantity;
            if (existingOrder.getRemark() == null || existingOrder.getRemark().isEmpty()) {
                // 获取原菜品名称
                MealDailyDish originalDish = dailyDishMapper.selectById(existingOrder.getDishId());
                String originalDishInfo = originalDish != null ? originalDish.getDishName() + " x" + (existingOrder.getQuantity() - quantity) : "";
                existingOrder.setRemark("菜品: " + originalDishInfo + ", " + dishInfo);
            } else {
                existingOrder.setRemark(existingOrder.getRemark() + ", " + dishInfo);
            }
            
            mealOrderMapper.updateById(existingOrder);
            
            result.put("orderId", existingOrder.getId());
            result.put("isMerged", true);
            result.put("serviceCode", existingOrder.getServiceCode());
            result.put("message", "已合并到现有订单");
            return result;
        }
        
        // 没有现有订单，创建新订单
        // 扣除积分
        pointsService.consume(elderlyId, totalAmount, "meal", null, "餐饮预约-" + dish.getDishName());
        
        // 创建订单
        MealOrder order = new MealOrder();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setElderlyId(elderlyId);
        order.setBookerId(userId);
        order.setDishId(request.getDishId());
        order.setDishDate(request.getDishDate());
        order.setMealType(request.getMealType());
        order.setQuantity(quantity);
        order.setAmount(totalAmount);
        
        // 不自动分配配送员，等待配送员接单时再绑定
        order.setWorkerId(null);
        order.setStatus(0);
        
        // 设置地址：优先使用传入的地址，否则获取老人的默认地址
        String address = request.getAddress();
        if (address == null || address.isEmpty()) {
            // 从用户信息获取默认地址
            com.elderly.entity.User elderly = userMapper.selectById(elderlyId);
            if (elderly != null && elderly.getAddress() != null) {
                address = elderly.getAddress();
            }
        }
        order.setAddress(address);
        order.setRemark(request.getRemark());
        
        // 生成6位服务码
        order.setServiceCode(String.valueOf((int) ((Math.random() * 900000) + 100000)));
        
        mealOrderMapper.insert(order);
        
        result.put("orderId", order.getId());
        result.put("isMerged", false);
        result.put("serviceCode", order.getServiceCode());
        result.put("message", "预约成功");
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancelOrder(Long orderId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        MealOrder order = mealOrderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getElderlyId().equals(userId) && !order.getBookerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() >= 2) {
            throw new RuntimeException("订单状态不允许取消");
        }
        
        // 获取送餐配置计算退款
        MealDeliveryConfig config = deliveryConfigMapper.selectByMealType(order.getMealType());
        LocalDateTime deliveryStart = order.getDishDate().atTime(config.getDeliveryStartTime());
        LocalDateTime now = LocalDateTime.now();
        
        long minutesBeforeDelivery = java.time.Duration.between(now, deliveryStart).toMinutes();
        
        int deductionRate = 0;
        String reason;
        if (minutesBeforeDelivery > 60) {
            // 送餐前>1小时：免费取消
            deductionRate = 0;
            reason = "免费取消";
        } else if (minutesBeforeDelivery > 10) {
            // 送餐前1小时~10分钟：扣5%
            deductionRate = 5;
            reason = "送餐前1小时内取消，扣除5%积分";
        } else {
            // 送餐前<10分钟：扣10%
            deductionRate = 10;
            reason = "送餐前10分钟内取消，扣除10%积分";
        }
        
        int deduction = order.getAmount() * deductionRate / 100;
        int refund = order.getAmount() - deduction;
        
        // 退还积分
        if (refund > 0) {
            pointsService.refund(order.getElderlyId(), refund, "meal", orderId, "餐饮订单取消退款");
        }
        
        // 更新订单状态
        order.setStatus(5); // 已取消
        order.setCancelTime(now);
        order.setCancelDeduction(deduction);
        order.setRefundAmount(refund);
        mealOrderMapper.updateById(order);
        
        result.put("deduction", deduction);
        result.put("refund", refund);
        result.put("reason", reason);
        return result;
    }

    @Override
    public List<MealOrder> getUserOrders(Long userId, Integer status) {
        LambdaQueryWrapper<MealOrder> wrapper = new LambdaQueryWrapper<MealOrder>()
                .eq(MealOrder::getElderlyId, userId)
                .eq(status != null, MealOrder::getStatus, status)
                .orderByDesc(MealOrder::getCreateTime);
        
        List<MealOrder> orders = mealOrderMapper.selectList(wrapper);
        // 填充菜品信息和配送员信息
        for (MealOrder order : orders) {
            order.setDish(dailyDishMapper.selectById(order.getDishId()));
            // 填充配送员信息
            if (order.getWorkerId() != null) {
                com.elderly.entity.User worker = userMapper.selectById(order.getWorkerId());
                if (worker != null) {
                    order.setWorkerName(worker.getName());
                    order.setWorkerPhone(worker.getPhone());
                }
            }
        }
        return orders;
    }
    
    @Override
    public boolean confirmOrder(Long orderId, Long userId) {
        MealOrder order = mealOrderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getElderlyId().equals(userId) && !order.getBookerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        // 状态2是已送达/待确认
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确，只有已送达状态才能确认完成");
        }
        order.setStatus(3); // 已完成
        return mealOrderMapper.updateById(order) > 0;
    }
    
    @Override
    public boolean disputeOrder(Long orderId, Long userId, String reason) {
        MealOrder order = mealOrderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getElderlyId().equals(userId) && !order.getBookerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        // 状态2是已送达/待确认
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确，只有已送达状态才能提交争议");
        }
        order.setStatus(4); // 争议中
        order.setRemark((order.getRemark() != null ? order.getRemark() + " | " : "") + "争议原因: " + reason);
        return mealOrderMapper.updateById(order) > 0;
    }
    
    @Override
    public boolean resolveDispute(Long orderId) {
        MealOrder order = mealOrderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 4) {
            throw new RuntimeException("订单状态不正确，只有争议中状态才能处理");
        }
        order.setStatus(3); // 已完成
        order.setRemark((order.getRemark() != null ? order.getRemark() + " | " : "") + "争议已处理");
        return mealOrderMapper.updateById(order) > 0;
    }
}
