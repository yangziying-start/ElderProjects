package com.elderly.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.dto.MealBookingRequest;
import com.elderly.dto.WeeklyMenuVO;
import com.elderly.entity.MealDailyDish;
import com.elderly.entity.MealDeliveryConfig;
import com.elderly.entity.MealOrder;
import com.elderly.entity.MealWeeklyMenu;
import com.elderly.entity.BuildingWorker;
import com.elderly.entity.Family;
import com.elderly.mapper.*;
import com.elderly.service.MealService;
import com.elderly.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealWeeklyMenuMapper weeklyMenuMapper;
    private final MealDailyDishMapper dailyDishMapper;
    private final MealDeliveryConfigMapper deliveryConfigMapper;
    private final MealOrderMapper mealOrderMapper;
    private final UserMapper userMapper;
    private final PointsService pointsService;
    private final FamilyMapper familyMapper;
    private final BuildingWorkerMapper buildingWorkerMapper;

    @Override
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
    public Long createOrder(MealBookingRequest request, Long userId) {
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
        
        // 扣除积分
        Long elderlyId = request.getElderlyId() != null ? request.getElderlyId() : userId;
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
        
        // 自动分配配送员：根据用户楼栋查找负责的配送员
        Long assignedWorkerId = autoAssignWorker(elderlyId);
        order.setWorkerId(assignedWorkerId);
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
        return order.getId();
    }
    
    /**
     * 根据用户楼栋自动分配配送员
     */
    private Long autoAssignWorker(Long elderlyId) {
        // 1. 获取用户的家庭信息
        Family family = familyMapper.selectByUserId(elderlyId);
        if (family == null || family.getBuilding() == null) {
            return null;
        }
        
        // 2. 获取该楼栋的主要配送员
        BuildingWorker primaryWorker = buildingWorkerMapper.selectPrimaryByBuilding(family.getBuilding());
        if (primaryWorker != null) {
            return primaryWorker.getWorkerId();
        }
        
        // 3. 如果没有主要配送员，获取该楼栋任意一个配送员
        List<BuildingWorker> workers = buildingWorkerMapper.selectByBuilding(family.getBuilding());
        if (!workers.isEmpty()) {
            return workers.get(0).getWorkerId();
        }
        
        return null;
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
        order.setStatus(3);
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
        // 填充菜品信息
        for (MealOrder order : orders) {
            order.setDish(dailyDishMapper.selectById(order.getDishId()));
        }
        return orders;
    }
}
