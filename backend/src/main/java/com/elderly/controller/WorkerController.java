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

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {

    private final UserService userService;
    private final CleaningOrderMapper cleaningOrderMapper;
    private final CleaningServiceMapper cleaningServiceMapper;
    private final MealOrderMapper mealOrderMapper;
    private final MealDailyDishMapper mealDailyDishMapper;
    private final MealDeliveryConfigMapper mealDeliveryConfigMapper;
    private final MedicalAppointmentMapper medicalAppointmentMapper;
    private final MedicalDoctorMapper medicalDoctorMapper;

    /**
     * 获取当前服务人员信息（包含workerType）
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getWorkerInfo(@RequestAttribute Long userId) {
        User user = userService.getById(userId);
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("name", user.getName());
        info.put("phone", user.getPhone());
        info.put("avatar", user.getAvatar());
        info.put("userType", user.getUserType());
        info.put("workerType", user.getWorkerType());
        info.put("workerTypeName", getWorkerTypeName(user.getWorkerType()));
        return Result.success(info);
    }
    
    private String getWorkerTypeName(Integer workerType) {
        if (workerType == null) return "服务人员";
        switch (workerType) {
            case 1: return "配送员";
            case 2: return "保洁员";
            case 3: return "医疗人员";
            default: return "服务人员";
        }
    }

    /**
     * 获取可接单任务列表（接单大厅）
     * 根据服务人员类型自动筛选：1-配送员只看餐饮，2-保洁员只看保洁，3-医疗人员只看医疗
     * @param type 服务类型：meal-餐饮, cleaning-保洁, medical-医疗, 不传则根据workerType自动筛选
     */
    @GetMapping("/available")
    public Result<List<OrderDetailVO>> getAvailableOrders(
            @RequestAttribute Long userId,
            @RequestParam(required = false) String type) {
        
        User worker = userService.getById(userId);
        Integer workerType = worker.getWorkerType();
        
        // 根据服务人员类型确定可查看的订单类型
        String effectiveType = determineEffectiveType(type, workerType);
        
        List<OrderDetailVO> allOrders = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // 1. 保洁订单 - 仅保洁员(workerType=2)可见
        if ((effectiveType == null && workerType != null && workerType == 2) || "cleaning".equals(effectiveType)) {
            List<CleaningOrder> cleaningOrders = cleaningOrderMapper.selectList(
                    new LambdaQueryWrapper<CleaningOrder>()
                            .eq(CleaningOrder::getWorkerId, userId)
                            .eq(CleaningOrder::getStatus, 0)
                            .isNull(CleaningOrder::getServiceCode)
                            .ge(CleaningOrder::getServiceDate, today)
                            .eq(CleaningOrder::getDeleted, 0)
                            .orderByAsc(CleaningOrder::getServiceDate));
            allOrders.addAll(convertCleaningToDetailList(cleaningOrders));
        }
        
        // 2. 餐饮订单 - 仅配送员(workerType=1)可见
        // 餐饮订单在创建时就已分配配送员和生成serviceCode，所以不需要检查serviceCode是否为空
        if ((effectiveType == null && workerType != null && workerType == 1) || "meal".equals(effectiveType)) {
            List<MealOrder> mealOrders = mealOrderMapper.selectList(
                    new LambdaQueryWrapper<MealOrder>()
                            .eq(MealOrder::getWorkerId, userId)
                            .eq(MealOrder::getStatus, 0)
                            .ge(MealOrder::getDishDate, today)
                            .eq(MealOrder::getDeleted, 0)
                            .orderByAsc(MealOrder::getDishDate));
            allOrders.addAll(convertMealToDetailList(mealOrders));
        }
        
        // 3. 医疗预约 - 仅医疗人员(workerType=3)可见
        if ((effectiveType == null && workerType != null && workerType == 3) || "medical".equals(effectiveType)) {
            List<Long> doctorIds = getDoctorIdsByUserId(userId);
            if (!doctorIds.isEmpty()) {
                List<MedicalAppointment> medicalOrders = medicalAppointmentMapper.selectList(
                        new LambdaQueryWrapper<MedicalAppointment>()
                                .in(MedicalAppointment::getDoctorId, doctorIds)
                                .eq(MedicalAppointment::getStatus, 0)
                                .isNull(MedicalAppointment::getServiceCode)
                                .ge(MedicalAppointment::getAppointmentDate, today)
                                .eq(MedicalAppointment::getDeleted, 0)
                                .orderByAsc(MedicalAppointment::getAppointmentDate));
                allOrders.addAll(convertMedicalToDetailList(medicalOrders));
            }
        }
        
        // 按预约时间排序
        allOrders.sort((a, b) -> {
            if (a.getAppointmentDate() == null) return 1;
            if (b.getAppointmentDate() == null) return -1;
            return a.getAppointmentDate().compareTo(b.getAppointmentDate());
        });
        
        return Result.success(allOrders);
    }
    
    /**
     * 根据服务人员类型确定有效的订单类型
     */
    private String determineEffectiveType(String requestType, Integer workerType) {
        if (requestType != null && !requestType.isEmpty()) {
            // 验证请求的类型是否与服务人员类型匹配
            if (workerType != null) {
                switch (workerType) {
                    case 1: // 配送员只能看餐饮
                        return "meal".equals(requestType) ? requestType : null;
                    case 2: // 保洁员只能看保洁
                        return "cleaning".equals(requestType) ? requestType : null;
                    case 3: // 医疗人员只能看医疗
                        return "medical".equals(requestType) ? requestType : null;
                }
            }
            return requestType;
        }
        return null; // 返回null表示根据workerType自动筛选
    }

    /**
     * 获取服务人员统计数据（根据服务人员类型筛选）
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(@RequestAttribute Long userId) {
        User worker = userService.getById(userId);
        Integer workerType = worker.getWorkerType();
        
        Map<String, Object> stats = new HashMap<>();
        
        long pendingCleaning = 0, pendingMeal = 0, pendingMedical = 0;
        long todayCleaning = 0, todayMeal = 0, todayMedical = 0;
        long completedCleaning = 0, completedMeal = 0, completedMedical = 0;
        
        LocalDate today = LocalDate.now();
        List<Long> doctorIds = getDoctorIdsByUserId(userId);
        
        // 保洁员(workerType=2)统计
        if (workerType == null || workerType == 2) {
            pendingCleaning = cleaningOrderMapper.selectCount(new LambdaQueryWrapper<CleaningOrder>()
                    .eq(CleaningOrder::getWorkerId, userId)
                    .eq(CleaningOrder::getStatus, 0)
                    .eq(CleaningOrder::getDeleted, 0));
            
            todayCleaning = cleaningOrderMapper.selectCount(new LambdaQueryWrapper<CleaningOrder>()
                    .eq(CleaningOrder::getWorkerId, userId)
                    .eq(CleaningOrder::getServiceDate, today)
                    .in(CleaningOrder::getStatus, 0, 1)
                    .eq(CleaningOrder::getDeleted, 0));
            
            completedCleaning = cleaningOrderMapper.selectCount(new LambdaQueryWrapper<CleaningOrder>()
                    .eq(CleaningOrder::getWorkerId, userId)
                    .in(CleaningOrder::getStatus, 2, 3)
                    .eq(CleaningOrder::getDeleted, 0));
        }
        
        // 配送员(workerType=1)统计
        if (workerType == null || workerType == 1) {
            pendingMeal = mealOrderMapper.selectCount(new LambdaQueryWrapper<MealOrder>()
                    .eq(MealOrder::getWorkerId, userId)
                    .eq(MealOrder::getStatus, 0)
                    .eq(MealOrder::getDeleted, 0));
            
            todayMeal = mealOrderMapper.selectCount(new LambdaQueryWrapper<MealOrder>()
                    .eq(MealOrder::getWorkerId, userId)
                    .eq(MealOrder::getDishDate, today)
                    .in(MealOrder::getStatus, 0, 1)
                    .eq(MealOrder::getDeleted, 0));
            
            completedMeal = mealOrderMapper.selectCount(new LambdaQueryWrapper<MealOrder>()
                    .eq(MealOrder::getWorkerId, userId)
                    .eq(MealOrder::getStatus, 2)
                    .eq(MealOrder::getDeleted, 0));
        }
        
        // 医疗人员(workerType=3)统计
        if ((workerType == null || workerType == 3) && !doctorIds.isEmpty()) {
            pendingMedical = medicalAppointmentMapper.selectCount(new LambdaQueryWrapper<MedicalAppointment>()
                    .in(MedicalAppointment::getDoctorId, doctorIds)
                    .eq(MedicalAppointment::getStatus, 0)
                    .eq(MedicalAppointment::getDeleted, 0));
            
            todayMedical = medicalAppointmentMapper.selectCount(new LambdaQueryWrapper<MedicalAppointment>()
                    .in(MedicalAppointment::getDoctorId, doctorIds)
                    .eq(MedicalAppointment::getAppointmentDate, today)
                    .in(MedicalAppointment::getStatus, 0, 1)
                    .eq(MedicalAppointment::getDeleted, 0));
            
            completedMedical = medicalAppointmentMapper.selectCount(new LambdaQueryWrapper<MedicalAppointment>()
                    .in(MedicalAppointment::getDoctorId, doctorIds)
                    .eq(MedicalAppointment::getStatus, 2)
                    .eq(MedicalAppointment::getDeleted, 0));
        }
        
        long pending = pendingCleaning + pendingMeal + pendingMedical;
        long todayTotal = todayCleaning + todayMeal + todayMedical;
        long completed = completedCleaning + completedMeal + completedMedical;
        
        stats.put("pending", pending);
        stats.put("today", todayTotal);
        stats.put("completed", completed);
        stats.put("workerType", workerType);
        
        return Result.success(stats);
    }

    /**
     * 获取我的工单列表（合并三种订单，根据服务人员类型筛选）
     * @param type 服务类型：meal-餐饮, cleaning-保洁, medical-医疗, 不传则根据workerType自动筛选
     */
    @GetMapping("/my-orders")
    public Result<List<OrderDetailVO>> getMyOrders(
            @RequestAttribute Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type) {
        
        User worker = userService.getById(userId);
        Integer workerType = worker.getWorkerType();
        String effectiveType = determineEffectiveType(type, workerType);
        
        List<OrderDetailVO> allOrders = new ArrayList<>();
        
        // 1. 保洁订单 - 仅保洁员可见
        if ((effectiveType == null && workerType != null && workerType == 2) || "cleaning".equals(effectiveType)) {
            LambdaQueryWrapper<CleaningOrder> cleaningWrapper = new LambdaQueryWrapper<CleaningOrder>()
                    .eq(CleaningOrder::getWorkerId, userId)
                    .eq(CleaningOrder::getDeleted, 0)
                    .eq(status != null, CleaningOrder::getStatus, status)
                    .orderByDesc(CleaningOrder::getCreateTime);
            List<CleaningOrder> cleaningOrders = cleaningOrderMapper.selectList(cleaningWrapper);
            allOrders.addAll(convertCleaningToDetailList(cleaningOrders));
        }
        
        // 2. 餐饮订单 - 仅配送员可见
        if ((effectiveType == null && workerType != null && workerType == 1) || "meal".equals(effectiveType)) {
            LambdaQueryWrapper<MealOrder> mealWrapper = new LambdaQueryWrapper<MealOrder>()
                    .eq(MealOrder::getWorkerId, userId)
                    .eq(MealOrder::getDeleted, 0)
                    .eq(status != null, MealOrder::getStatus, status)
                    .orderByDesc(MealOrder::getCreateTime);
            List<MealOrder> mealOrders = mealOrderMapper.selectList(mealWrapper);
            allOrders.addAll(convertMealToDetailList(mealOrders));
        }
        
        // 3. 医疗预约（通过医生关联）- 仅医疗人员可见
        if ((effectiveType == null && workerType != null && workerType == 3) || "medical".equals(effectiveType)) {
            List<Long> doctorIds = getDoctorIdsByUserId(userId);
            if (!doctorIds.isEmpty()) {
                LambdaQueryWrapper<MedicalAppointment> medicalWrapper = new LambdaQueryWrapper<MedicalAppointment>()
                        .in(MedicalAppointment::getDoctorId, doctorIds)
                        .eq(MedicalAppointment::getDeleted, 0)
                        .eq(status != null, MedicalAppointment::getStatus, status)
                        .orderByDesc(MedicalAppointment::getCreateTime);
                List<MedicalAppointment> medicalOrders = medicalAppointmentMapper.selectList(medicalWrapper);
                allOrders.addAll(convertMedicalToDetailList(medicalOrders));
            }
        }
        
        // 按创建时间排序
        allOrders.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });
        
        return Result.success(allOrders);
    }

    /**
     * 获取我的日程（按日期分组，根据服务人员类型筛选）
     * @param type 服务类型：meal-餐饮, cleaning-保洁, medical-医疗, 不传则根据workerType自动筛选
     */
    @GetMapping("/schedule")
    public Result<List<Map<String, Object>>> getSchedule(
            @RequestAttribute Long userId,
            @RequestParam(required = false) String type) {
        
        User worker = userService.getById(userId);
        Integer workerType = worker.getWorkerType();
        String effectiveType = determineEffectiveType(type, workerType);
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);
        
        List<OrderDetailVO> allOrders = new ArrayList<>();
        
        // 1. 保洁订单 - 仅保洁员可见
        if ((effectiveType == null && workerType != null && workerType == 2) || "cleaning".equals(effectiveType)) {
            List<CleaningOrder> cleaningOrders = cleaningOrderMapper.selectList(
                    new LambdaQueryWrapper<CleaningOrder>()
                            .eq(CleaningOrder::getWorkerId, userId)
                            .ge(CleaningOrder::getServiceDate, today)
                            .le(CleaningOrder::getServiceDate, endDate)
                            .in(CleaningOrder::getStatus, 0, 1, 2)
                            .eq(CleaningOrder::getDeleted, 0));
            allOrders.addAll(convertCleaningToDetailList(cleaningOrders));
        }
        
        // 2. 餐饮订单 - 仅配送员可见
        if ((effectiveType == null && workerType != null && workerType == 1) || "meal".equals(effectiveType)) {
            List<MealOrder> mealOrders = mealOrderMapper.selectList(
                    new LambdaQueryWrapper<MealOrder>()
                            .eq(MealOrder::getWorkerId, userId)
                            .ge(MealOrder::getDishDate, today)
                            .le(MealOrder::getDishDate, endDate)
                            .in(MealOrder::getStatus, 0, 1)
                            .eq(MealOrder::getDeleted, 0));
            allOrders.addAll(convertMealToDetailList(mealOrders));
        }
        
        // 3. 医疗预约 - 仅医疗人员可见
        if ((effectiveType == null && workerType != null && workerType == 3) || "medical".equals(effectiveType)) {
            List<Long> doctorIds = getDoctorIdsByUserId(userId);
            if (!doctorIds.isEmpty()) {
                List<MedicalAppointment> medicalOrders = medicalAppointmentMapper.selectList(
                        new LambdaQueryWrapper<MedicalAppointment>()
                                .in(MedicalAppointment::getDoctorId, doctorIds)
                                .ge(MedicalAppointment::getAppointmentDate, today)
                                .le(MedicalAppointment::getAppointmentDate, endDate)
                                .in(MedicalAppointment::getStatus, 0, 1)
                                .eq(MedicalAppointment::getDeleted, 0));
                allOrders.addAll(convertMedicalToDetailList(medicalOrders));
            }
        }
        
        // 按日期分组
        Map<String, List<OrderDetailVO>> groupedOrders = allOrders.stream()
                .filter(o -> o.getAppointmentDate() != null)
                .collect(Collectors.groupingBy(OrderDetailVO::getAppointmentDate));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<OrderDetailVO>> entry : groupedOrders.entrySet()) {
            Map<String, Object> daySchedule = new HashMap<>();
            LocalDate date = LocalDate.parse(entry.getKey());
            daySchedule.put("date", date.toString());
            daySchedule.put("dateText", formatDate(date));
            daySchedule.put("isToday", date.equals(today));
            daySchedule.put("orders", entry.getValue());
            result.add(daySchedule);
        }
        
        result.sort((a, b) -> ((String)a.get("date")).compareTo((String)b.get("date")));
        
        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/order/{id}")
    public Result<OrderDetailVO> getOrderDetail(
            @RequestAttribute Long userId,
            @PathVariable Long id,
            @RequestParam(defaultValue = "cleaning") String type) {
        
        switch (type) {
            case "meal":
                MealOrder mealOrder = mealOrderMapper.selectById(id);
                if (mealOrder == null || mealOrder.getDeleted() == 1) {
                    throw new RuntimeException("订单不存在");
                }
                if (!userId.equals(mealOrder.getWorkerId())) {
                    throw new RuntimeException("无权查看此订单");
                }
                return Result.success(convertMealToDetail(mealOrder));
                
            case "medical":
                MedicalAppointment medicalOrder = medicalAppointmentMapper.selectById(id);
                if (medicalOrder == null || medicalOrder.getDeleted() == 1) {
                    throw new RuntimeException("订单不存在");
                }
                List<Long> doctorIds = getDoctorIdsByUserId(userId);
                if (!doctorIds.contains(medicalOrder.getDoctorId())) {
                    throw new RuntimeException("无权查看此订单");
                }
                return Result.success(convertMedicalToDetail(medicalOrder));
                
            default: // cleaning
                CleaningOrder cleaningOrder = cleaningOrderMapper.selectById(id);
                if (cleaningOrder == null || cleaningOrder.getDeleted() == 1) {
                    throw new RuntimeException("订单不存在");
                }
                if (!userId.equals(cleaningOrder.getWorkerId())) {
                    throw new RuntimeException("无权查看此订单");
                }
                return Result.success(convertCleaningToDetail(cleaningOrder));
        }
    }

    /**
     * 服务码验证打卡
     */
    @PostMapping("/verify-code")
    public Result<Boolean> verifyServiceCode(
            @RequestAttribute Long userId,
            @RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String serviceCode = (String) body.get("serviceCode");
        String type = body.get("type") != null ? (String) body.get("type") : "cleaning";
        
        switch (type) {
            case "meal":
                MealOrder mealOrder = mealOrderMapper.selectById(orderId);
                if (mealOrder == null || !userId.equals(mealOrder.getWorkerId())) {
                    throw new RuntimeException("无权操作此订单");
                }
                if (mealOrder.getStatus() != 0) {
                    throw new RuntimeException("订单状态不正确");
                }
                // 验证服务码
                if (mealOrder.getServiceCode() != null && !mealOrder.getServiceCode().isEmpty()) {
                    if (!serviceCode.equals(mealOrder.getServiceCode())) {
                        throw new RuntimeException("服务码错误，请重新输入");
                    }
                }
                // 检测迟到 - 根据配送时间配置
                LocalDateTime mealNow = LocalDateTime.now();
                MealDeliveryConfig config = mealDeliveryConfigMapper.selectByMealType(mealOrder.getMealType());
                if (config != null) {
                    LocalDateTime scheduledDeliveryEnd = mealOrder.getDishDate().atTime(config.getDeliveryEndTime());
                    if (mealNow.isAfter(scheduledDeliveryEnd)) {
                        long lateMinutes = java.time.Duration.between(scheduledDeliveryEnd, mealNow).toMinutes();
                        mealOrder.setIsLate(1);
                        mealOrder.setLateMinutes((int) lateMinutes);
                    } else {
                        mealOrder.setIsLate(0);
                        mealOrder.setLateMinutes(0);
                    }
                }
                mealOrder.setStatus(1); // 配送中
                mealOrder.setUpdateTime(mealNow);
                mealOrderMapper.updateById(mealOrder);
                return Result.success(true);
                
            case "medical":
                MedicalAppointment medicalOrder = medicalAppointmentMapper.selectById(orderId);
                if (medicalOrder == null || medicalOrder.getDeleted() == 1) {
                    throw new RuntimeException("订单不存在");
                }
                List<Long> doctorIds = getDoctorIdsByUserId(userId);
                if (!doctorIds.contains(medicalOrder.getDoctorId())) {
                    throw new RuntimeException("无权操作此订单");
                }
                if (medicalOrder.getStatus() != 0) {
                    throw new RuntimeException("订单状态不正确");
                }
                // 验证服务码
                if (medicalOrder.getServiceCode() != null && !medicalOrder.getServiceCode().isEmpty()) {
                    if (!serviceCode.equals(medicalOrder.getServiceCode())) {
                        throw new RuntimeException("服务码错误，请重新输入");
                    }
                }
                LocalDateTime medicalNow = LocalDateTime.now();
                medicalOrder.setStatus(1); // 巡诊中
                medicalOrder.setVisitTime(medicalNow);
                medicalOrder.setUpdateTime(medicalNow);
                // 医疗预约暂不检测迟到（基于排队号，由医生控制进度）
                medicalOrder.setIsLate(0);
                medicalOrder.setLateMinutes(0);
                medicalAppointmentMapper.updateById(medicalOrder);
                return Result.success(true);
                
            default: // cleaning
                CleaningOrder cleaningOrder = cleaningOrderMapper.selectById(orderId);
                if (cleaningOrder == null || cleaningOrder.getDeleted() == 1) {
                    throw new RuntimeException("订单不存在");
                }
                if (!userId.equals(cleaningOrder.getWorkerId())) {
                    throw new RuntimeException("无权操作此订单");
                }
                if (cleaningOrder.getStatus() != 0) {
                    throw new RuntimeException("订单状态不正确");
                }
                // 验证服务码
                if (cleaningOrder.getServiceCode() != null && !cleaningOrder.getServiceCode().isEmpty()) {
                    if (!serviceCode.equals(cleaningOrder.getServiceCode())) {
                        throw new RuntimeException("服务码错误，请重新输入");
                    }
                }
                // 检测迟到
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime scheduledTime = cleaningOrder.getServiceDate().atTime(cleaningOrder.getStartTime());
                if (now.isAfter(scheduledTime)) {
                    long lateMinutes = java.time.Duration.between(scheduledTime, now).toMinutes();
                    cleaningOrder.setIsLate(1);
                    cleaningOrder.setLateMinutes((int) lateMinutes);
                } else {
                    cleaningOrder.setIsLate(0);
                    cleaningOrder.setLateMinutes(0);
                }
                cleaningOrder.setStatus(1);
                cleaningOrder.setActualStartTime(now);
                cleaningOrder.setUpdateTime(now);
                cleaningOrderMapper.updateById(cleaningOrder);
                return Result.success(true);
        }
    }

    /**
     * 服务人员接单（生成服务码）
     */
    @PostMapping("/accept")
    public Result<Map<String, Object>> acceptOrder(
            @RequestAttribute Long userId,
            @RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String type = body.get("type") != null ? (String) body.get("type") : "cleaning";
        
        String serviceCode = cn.hutool.core.util.RandomUtil.randomNumbers(6);
        Map<String, Object> result = new HashMap<>();
        result.put("serviceCode", serviceCode);
        
        switch (type) {
            case "meal":
                MealOrder mealOrder = mealOrderMapper.selectById(orderId);
                if (mealOrder == null || mealOrder.getDeleted() == 1) {
                    throw new RuntimeException("订单不存在");
                }
                if (!userId.equals(mealOrder.getWorkerId())) {
                    throw new RuntimeException("无权操作此订单");
                }
                if (mealOrder.getServiceCode() != null && !mealOrder.getServiceCode().isEmpty()) {
                    throw new RuntimeException("订单已接单");
                }
                mealOrder.setServiceCode(serviceCode);
                mealOrder.setUpdateTime(LocalDateTime.now());
                mealOrderMapper.updateById(mealOrder);
                return Result.success(result);
                
            case "medical":
                MedicalAppointment medicalOrder = medicalAppointmentMapper.selectById(orderId);
                if (medicalOrder == null || medicalOrder.getDeleted() == 1) {
                    throw new RuntimeException("订单不存在");
                }
                List<Long> doctorIds = getDoctorIdsByUserId(userId);
                if (!doctorIds.contains(medicalOrder.getDoctorId())) {
                    throw new RuntimeException("无权操作此订单");
                }
                if (medicalOrder.getServiceCode() != null && !medicalOrder.getServiceCode().isEmpty()) {
                    throw new RuntimeException("订单已接单");
                }
                medicalOrder.setServiceCode(serviceCode);
                medicalOrder.setUpdateTime(LocalDateTime.now());
                medicalAppointmentMapper.updateById(medicalOrder);
                return Result.success(result);
                
            default: // cleaning
                CleaningOrder cleaningOrder = cleaningOrderMapper.selectById(orderId);
                if (cleaningOrder == null || cleaningOrder.getDeleted() == 1) {
                    throw new RuntimeException("订单不存在");
                }
                if (!userId.equals(cleaningOrder.getWorkerId())) {
                    throw new RuntimeException("无权操作此订单");
                }
                if (cleaningOrder.getServiceCode() != null && !cleaningOrder.getServiceCode().isEmpty()) {
                    throw new RuntimeException("订单已接单");
                }
                cleaningOrder.setServiceCode(serviceCode);
                cleaningOrder.setUpdateTime(LocalDateTime.now());
                cleaningOrderMapper.updateById(cleaningOrder);
                return Result.success(result);
        }
    }

    /**
     * 完成服务
     */
    @PostMapping("/complete")
    public Result<Boolean> completeService(
            @RequestAttribute Long userId,
            @RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String evidence = (String) body.get("evidence");
        String type = body.get("type") != null ? (String) body.get("type") : "cleaning";
        
        switch (type) {
            case "meal":
                MealOrder mealOrder = mealOrderMapper.selectById(orderId);
                if (mealOrder == null || !userId.equals(mealOrder.getWorkerId())) {
                    throw new RuntimeException("无权操作此订单");
                }
                mealOrder.setStatus(2); // 已送达
                mealOrder.setUpdateTime(LocalDateTime.now());
                mealOrderMapper.updateById(mealOrder);
                return Result.success(true);
                
            case "medical":
                MedicalAppointment medicalOrder = medicalAppointmentMapper.selectById(orderId);
                if (medicalOrder == null) {
                    throw new RuntimeException("订单不存在");
                }
                String diagnosis = body.get("diagnosis") != null ? (String) body.get("diagnosis") : "";
                String prescription = body.get("prescription") != null ? (String) body.get("prescription") : "";
                medicalOrder.setStatus(2); // 已完成
                medicalOrder.setDiagnosis(diagnosis);
                medicalOrder.setPrescription(prescription);
                medicalOrder.setUpdateTime(LocalDateTime.now());
                medicalAppointmentMapper.updateById(medicalOrder);
                return Result.success(true);
                
            default: // cleaning
                CleaningOrder cleaningOrder = cleaningOrderMapper.selectById(orderId);
                if (cleaningOrder == null || cleaningOrder.getDeleted() == 1) {
                    throw new RuntimeException("订单不存在");
                }
                if (!userId.equals(cleaningOrder.getWorkerId())) {
                    throw new RuntimeException("无权操作此订单");
                }
                if (cleaningOrder.getStatus() != 1) {
                    throw new RuntimeException("请先验证服务码开始服务");
                }
                cleaningOrder.setEvidence(evidence);
                cleaningOrder.setStatus(2);
                cleaningOrder.setActualEndTime(LocalDateTime.now());
                cleaningOrder.setUpdateTime(LocalDateTime.now());
                cleaningOrderMapper.updateById(cleaningOrder);
                return Result.success(true);
        }
    }

    // ========== 辅助方法 ==========
    
    private List<Long> getDoctorIdsByUserId(Long userId) {
        List<MedicalDoctor> doctors = medicalDoctorMapper.selectList(
                new LambdaQueryWrapper<MedicalDoctor>()
                        .eq(MedicalDoctor::getUserId, userId)
                        .eq(MedicalDoctor::getDeleted, 0));
        return doctors.stream().map(MedicalDoctor::getId).collect(Collectors.toList());
    }

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
        vo.setRemark(order.getRemark());
        vo.setEvidence(order.getEvidence());
        vo.setServiceCode(order.getServiceCode());
        vo.setCreateTime(order.getCreateTime());
        
        if (order.getServiceDate() != null && order.getStartTime() != null) {
            vo.setAppointmentTime(order.getServiceDate() + " " + order.getStartTime() + "-" + order.getEndTime());
            vo.setAppointmentDate(order.getServiceDate().toString());
        }
        
        CleaningService service = cleaningServiceMapper.selectById(order.getServiceId());
        if (service != null) {
            vo.setServiceName("保洁-" + service.getName());
        }
        
        User elderly = userService.getById(order.getElderlyId());
        if (elderly != null) {
            vo.setElderlyName(elderly.getName());
            vo.setElderlyPhone(elderly.getPhone());
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
        vo.setRemark(order.getRemark());
        vo.setServiceCode(order.getServiceCode());
        vo.setCreateTime(order.getCreateTime());
        
        if (order.getDishDate() != null) {
            String mealTypeText = getMealTypeText(order.getMealType());
            vo.setAppointmentTime(order.getDishDate() + " " + mealTypeText);
            vo.setAppointmentDate(order.getDishDate().toString());
        }
        
        MealDailyDish dish = mealDailyDishMapper.selectById(order.getDishId());
        if (dish != null) {
            vo.setServiceName("餐饮-" + dish.getDishName());
        }
        
        User elderly = userService.getById(order.getElderlyId());
        if (elderly != null) {
            vo.setElderlyName(elderly.getName());
            vo.setElderlyPhone(elderly.getPhone());
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
        vo.setRemark(order.getSymptoms());
        vo.setServiceCode(order.getServiceCode());
        vo.setCreateTime(order.getCreateTime());
        
        if (order.getAppointmentDate() != null) {
            String typeText = order.getAppointmentType() == 1 ? "日间巡诊" : "夜间急诊";
            vo.setAppointmentTime(order.getAppointmentDate() + " " + typeText + " 第" + order.getQueueNumber() + "号");
            vo.setAppointmentDate(order.getAppointmentDate().toString());
        }
        
        MedicalDoctor doctor = medicalDoctorMapper.selectById(order.getDoctorId());
        if (doctor != null) {
            vo.setServiceName("医疗-" + doctor.getName() + "(" + doctor.getSpecialty() + ")");
        }
        
        User elderly = userService.getById(order.getElderlyId());
        if (elderly != null) {
            vo.setElderlyName(elderly.getName());
            vo.setElderlyPhone(elderly.getPhone());
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

    private String getMealTypeText(Integer mealType) {
        if (mealType == null) return "";
        switch (mealType) {
            case 1: return "早餐";
            case 2: return "午餐";
            case 3: return "晚餐";
            default: return "";
        }
    }

    private String formatDate(LocalDate date) {
        if (date.equals(LocalDate.now())) {
            return "今天";
        } else if (date.equals(LocalDate.now().plusDays(1))) {
            return "明天";
        } else {
            return date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
        }
    }
}
