package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.common.Result;
import com.elderly.dto.OrderDetailVO;
import com.elderly.entity.*;
import com.elderly.mapper.*;
import com.elderly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理后台 - 仪表盘统计
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserService userService;
    private final CleaningOrderMapper cleaningOrderMapper;
    private final CleaningServiceMapper cleaningServiceMapper;
    private final MealOrderMapper mealOrderMapper;
    private final MealDailyDishMapper mealDailyDishMapper;
    private final MedicalAppointmentMapper medicalAppointmentMapper;
    private final MedicalDoctorMapper medicalDoctorMapper;
    private final EmergencyCallMapper emergencyCallMapper;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户统计
        long userCount = userService.count(new LambdaQueryWrapper<User>()
                .ne(User::getUserType, 4));
//                .eq(User::getDeleted, 0)); // 排除管理员
        long elderlyCount = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUserType, 1));
//                .eq(User::getDeleted, 0));
        long childCount = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUserType, 2));
//                .eq(User::getDeleted, 0));
        long workerCount = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUserType, 3));
//                .eq(User::getDeleted, 0));
        
        // 订单统计 - 合并三种订单
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        
        // 保洁订单
        long cleaningTotal = cleaningOrderMapper.selectCount(new LambdaQueryWrapper<CleaningOrder>()
                .eq(CleaningOrder::getDeleted, 0));
        long cleaningToday = cleaningOrderMapper.selectCount(new LambdaQueryWrapper<CleaningOrder>()
                .ge(CleaningOrder::getCreateTime, todayStart)
                .eq(CleaningOrder::getDeleted, 0));
        long cleaningPending = cleaningOrderMapper.selectCount(new LambdaQueryWrapper<CleaningOrder>()
                .eq(CleaningOrder::getStatus, 0)
                .eq(CleaningOrder::getDeleted, 0));
        long cleaningCompleted = cleaningOrderMapper.selectCount(new LambdaQueryWrapper<CleaningOrder>()
                .eq(CleaningOrder::getStatus, 3)
                .eq(CleaningOrder::getDeleted, 0));
        
        // 餐饮订单
        long mealTotal = mealOrderMapper.selectCount(new LambdaQueryWrapper<MealOrder>()
                .eq(MealOrder::getDeleted, 0));
        long mealToday = mealOrderMapper.selectCount(new LambdaQueryWrapper<MealOrder>()
                .ge(MealOrder::getCreateTime, todayStart)
                .eq(MealOrder::getDeleted, 0));
        long mealPending = mealOrderMapper.selectCount(new LambdaQueryWrapper<MealOrder>()
                .eq(MealOrder::getStatus, 0)
                .eq(MealOrder::getDeleted, 0));
        long mealCompleted = mealOrderMapper.selectCount(new LambdaQueryWrapper<MealOrder>()
                .eq(MealOrder::getStatus, 2)
                .eq(MealOrder::getDeleted, 0));
        
        // 医疗预约
        long medicalTotal = medicalAppointmentMapper.selectCount(new LambdaQueryWrapper<MedicalAppointment>()
                .eq(MedicalAppointment::getDeleted, 0));
        long medicalToday = medicalAppointmentMapper.selectCount(new LambdaQueryWrapper<MedicalAppointment>()
                .ge(MedicalAppointment::getCreateTime, todayStart)
                .eq(MedicalAppointment::getDeleted, 0));
        long medicalPending = medicalAppointmentMapper.selectCount(new LambdaQueryWrapper<MedicalAppointment>()
                .eq(MedicalAppointment::getStatus, 0)
                .eq(MedicalAppointment::getDeleted, 0));
        long medicalCompleted = medicalAppointmentMapper.selectCount(new LambdaQueryWrapper<MedicalAppointment>()
                .eq(MedicalAppointment::getStatus, 2)
                .eq(MedicalAppointment::getDeleted, 0));
        
        long orderCount = cleaningTotal + mealTotal + medicalTotal;
        long todayOrders = cleaningToday + mealToday + medicalToday;
        long pendingOrders = cleaningPending + mealPending + medicalPending;
        long completedOrders = cleaningCompleted + mealCompleted + medicalCompleted;
        
        // 紧急呼叫统计
        long pendingEmergency = emergencyCallMapper.selectCount(new LambdaQueryWrapper<EmergencyCall>()
                .eq(EmergencyCall::getStatus, 0));
        long todayEmergency = emergencyCallMapper.selectCount(new LambdaQueryWrapper<EmergencyCall>()
                .ge(EmergencyCall::getTriggerTime, todayStart));
        
        stats.put("userCount", userCount);
        stats.put("elderlyCount", elderlyCount);
        stats.put("childCount", childCount);
        stats.put("workerCount", workerCount);
        stats.put("orderCount", orderCount);
        stats.put("todayOrders", todayOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("pendingEmergency", pendingEmergency);
        stats.put("todayEmergency", todayEmergency);
        
        return Result.success(stats);
    }

    /**
     * 获取最近订单（合并三种订单类型）
     */
    @GetMapping("/recent-orders")
    public Result<List<OrderDetailVO>> getRecentOrders() {
        List<OrderDetailVO> allOrders = new ArrayList<>();
        
        // 1. 保洁订单
        List<CleaningOrder> cleaningOrders = cleaningOrderMapper.selectList(
                new LambdaQueryWrapper<CleaningOrder>()
                        .eq(CleaningOrder::getDeleted, 0)
                        .orderByDesc(CleaningOrder::getCreateTime)
                        .last("LIMIT 10"));
        allOrders.addAll(convertCleaningToDetailList(cleaningOrders));
        
        // 2. 餐饮订单
        List<MealOrder> mealOrders = mealOrderMapper.selectList(
                new LambdaQueryWrapper<MealOrder>()
                        .eq(MealOrder::getDeleted, 0)
                        .orderByDesc(MealOrder::getCreateTime)
                        .last("LIMIT 10"));
        allOrders.addAll(convertMealToDetailList(mealOrders));
        
        // 3. 医疗预约
        List<MedicalAppointment> medicalOrders = medicalAppointmentMapper.selectList(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getDeleted, 0)
                        .orderByDesc(MedicalAppointment::getCreateTime)
                        .last("LIMIT 10"));
        allOrders.addAll(convertMedicalToDetailList(medicalOrders));
        
        // 按创建时间排序，取前10条
        allOrders.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });
        
        return Result.success(allOrders.stream().limit(10).collect(Collectors.toList()));
    }

    // ========== 辅助方法 ==========
    
    private List<OrderDetailVO> convertCleaningToDetailList(List<CleaningOrder> orders) {
        return orders.stream().map(this::convertCleaningToDetail).collect(Collectors.toList());
    }

    private OrderDetailVO convertCleaningToDetail(CleaningOrder order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderType("cleaning");
        vo.setStatus(order.getStatus());
        vo.setAmount(order.getAmount() != null ? new java.math.BigDecimal(order.getAmount()) : null);
        vo.setAddress(order.getAddress());
        vo.setCreateTime(order.getCreateTime());
        
        CleaningService service = cleaningServiceMapper.selectById(order.getServiceId());
        if (service != null) {
            vo.setServiceName("保洁-" + service.getName());
        }
        
        User elderly = userService.getById(order.getElderlyId());
        if (elderly != null) {
            vo.setElderlyName(elderly.getName());
        }
        
        vo.setStatusText(getCleaningStatusText(order.getStatus()));
        return vo;
    }

    private List<OrderDetailVO> convertMealToDetailList(List<MealOrder> orders) {
        return orders.stream().map(this::convertMealToDetail).collect(Collectors.toList());
    }

    private OrderDetailVO convertMealToDetail(MealOrder order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderType("meal");
        vo.setStatus(order.getStatus());
        vo.setAmount(order.getAmount() != null ? new java.math.BigDecimal(order.getAmount()) : null);
        vo.setAddress(order.getAddress());
        vo.setCreateTime(order.getCreateTime());
        
        MealDailyDish dish = mealDailyDishMapper.selectById(order.getDishId());
        // 处理合并订单：如果remark以"菜品:"开头，显示remark中的菜品信息
        if (order.getRemark() != null && order.getRemark().startsWith("菜品:")) {
            vo.setServiceName("餐饮-" + order.getRemark().replace("菜品: ", ""));
        } else if (dish != null) {
            vo.setServiceName("餐饮-" + dish.getDishName());
        }
        
        User elderly = userService.getById(order.getElderlyId());
        if (elderly != null) {
            vo.setElderlyName(elderly.getName());
        }
        
        vo.setStatusText(getMealStatusText(order.getStatus()));
        return vo;
    }

    private List<OrderDetailVO> convertMedicalToDetailList(List<MedicalAppointment> orders) {
        return orders.stream().map(this::convertMedicalToDetail).collect(Collectors.toList());
    }

    private OrderDetailVO convertMedicalToDetail(MedicalAppointment order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderType("medical");
        vo.setStatus(order.getStatus());
        vo.setAddress(order.getAddress());
        vo.setCreateTime(order.getCreateTime());
        
        MedicalDoctor doctor = medicalDoctorMapper.selectById(order.getDoctorId());
        if (doctor != null) {
            vo.setServiceName("医疗-" + doctor.getName() + "(" + doctor.getSpecialty() + ")");
        }
        
        User elderly = userService.getById(order.getElderlyId());
        if (elderly != null) {
            vo.setElderlyName(elderly.getName());
        }
        
        vo.setStatusText(getMedicalStatusText(order.getStatus()));
        return vo;
    }

    private String getCleaningStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待服务";
            case 1: return "服务中";
            case 2: return "待确认";
            case 3: return "已完成";
            case 4: return "已取消";
            case 5: return "争议中";
            default: return "未知";
        }
    }

    private String getMealStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待配送";
            case 1: return "配送中";
            case 2: return "已送达";
            case 3: return "已取消";
            default: return "未知";
        }
    }

    private String getMedicalStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "排队中";
            case 1: return "巡诊中";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "未知";
        }
    }
}
