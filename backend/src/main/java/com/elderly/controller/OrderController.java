package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.dto.BookingRequest;
import com.elderly.dto.OrderDetailVO;
import com.elderly.entity.*;
import com.elderly.mapper.CleaningOrderMapper;
import com.elderly.mapper.CleaningServiceMapper;
import com.elderly.mapper.MealDailyDishMapper;
import com.elderly.mapper.MealOrderMapper;
import com.elderly.mapper.MedicalAppointmentMapper;
import com.elderly.mapper.MedicalDoctorMapper;
import com.elderly.service.OrderService;
import com.elderly.service.ServiceItemService;
import com.elderly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ServiceItemService serviceItemService;
    private final UserService userService;
    private final CleaningOrderMapper cleaningOrderMapper;
    private final CleaningServiceMapper cleaningServiceMapper;
    private final MealOrderMapper mealOrderMapper;
    private final MealDailyDishMapper mealDailyDishMapper;
    private final MedicalAppointmentMapper medicalAppointmentMapper;
    private final MedicalDoctorMapper medicalDoctorMapper;

    /**
     * 创建预约订单（新版，带容量检查和医疗健康信息）
     */
    @PostMapping("/book")
    public Result<Long> bookService(@RequestBody BookingRequest request, @RequestAttribute Long userId) {
        return Result.success(orderService.createOrder(request, userId));
    }

    @PostMapping
    public Result<Long> createOrder(@RequestBody Order order, @RequestAttribute Long userId) {
        order.setBookerId(userId);
        if (order.getElderlyId() == null) {
            order.setElderlyId(userId);
        }
        return Result.success(orderService.createOrder(order));
    }

    @GetMapping("/my")
    public Result<List<OrderDetailVO>> myOrders(@RequestAttribute Long userId, @RequestParam(required = false) Integer status) {
        List<OrderDetailVO> allOrders = new ArrayList<>();
        
        // 1. 查询通用服务订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getElderlyId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        List<Order> orders = orderService.list(wrapper);
        allOrders.addAll(orders.stream().map(this::convertToDetail).collect(Collectors.toList()));
        
        // 2. 查询保洁订单
        LambdaQueryWrapper<CleaningOrder> cleaningWrapper = new LambdaQueryWrapper<CleaningOrder>()
                .eq(CleaningOrder::getElderlyId, userId)
                .eq(status != null, CleaningOrder::getStatus, status)
                .orderByDesc(CleaningOrder::getCreateTime);
        List<CleaningOrder> cleaningOrders = cleaningOrderMapper.selectList(cleaningWrapper);
        for (CleaningOrder co : cleaningOrders) {
            OrderDetailVO vo = new OrderDetailVO();
            vo.setId(co.getId());
            vo.setOrderNo(co.getOrderNo());
            vo.setOrderType("cleaning");
            vo.setStatus(co.getStatus());
            vo.setAmount(co.getAmount() != null ? BigDecimal.valueOf(co.getAmount()) : null);
            vo.setAddress(co.getAddress());
            vo.setRemark(co.getRemark());
            vo.setServiceCode(co.getServiceCode());
            vo.setCreateTime(co.getCreateTime());
            vo.setAppointmentTime(co.getServiceDate() + " " + co.getStartTime() + "-" + co.getEndTime());
            vo.setAppointmentDate(co.getServiceDate() != null ? co.getServiceDate().toString() : null);
            
            CleaningService cs = cleaningServiceMapper.selectById(co.getServiceId());
            vo.setServiceName(cs != null ? "保洁-" + cs.getName() : "保洁服务");
            
            if (co.getWorkerId() != null) {
                User worker = userService.getById(co.getWorkerId());
                if (worker != null) {
                    vo.setWorkerName(worker.getName());
                    vo.setWorkerPhone(worker.getPhone());
                }
            }
            
            vo.setStatusText(getCleaningStatusText(co.getStatus()));
            allOrders.add(vo);
        }
        
        // 3. 查询餐饮订单
        LambdaQueryWrapper<MealOrder> mealWrapper = new LambdaQueryWrapper<MealOrder>()
                .eq(MealOrder::getElderlyId, userId)
                .eq(status != null, MealOrder::getStatus, status)
                .orderByDesc(MealOrder::getCreateTime);
        List<MealOrder> mealOrders = mealOrderMapper.selectList(mealWrapper);
        for (MealOrder mo : mealOrders) {
            OrderDetailVO vo = new OrderDetailVO();
            vo.setId(mo.getId());
            vo.setOrderNo(mo.getOrderNo());
            vo.setOrderType("meal");
            vo.setStatus(mo.getStatus());
            vo.setAmount(mo.getAmount() != null ? BigDecimal.valueOf(mo.getAmount()) : null);
            vo.setAddress(mo.getAddress());
            vo.setRemark(mo.getRemark());
            vo.setServiceCode(mo.getServiceCode());
            vo.setCreateTime(mo.getCreateTime());
            vo.setAppointmentDate(mo.getDishDate() != null ? mo.getDishDate().toString() : null);
            vo.setAppointmentTime(mo.getDishDate() + " " + getMealTypeText(mo.getMealType()));
            
            MealDailyDish dish = mealDailyDishMapper.selectById(mo.getDishId());
            vo.setServiceName(dish != null ? "餐饮-" + dish.getDishName() : "餐饮配送");
            
            if (mo.getWorkerId() != null) {
                User worker = userService.getById(mo.getWorkerId());
                if (worker != null) {
                    vo.setWorkerName(worker.getName());
                    vo.setWorkerPhone(worker.getPhone());
                }
            }
            
            vo.setStatusText(getMealStatusText(mo.getStatus()));
            allOrders.add(vo);
        }
        
        // 4. 查询医疗预约
        LambdaQueryWrapper<MedicalAppointment> medicalWrapper = new LambdaQueryWrapper<MedicalAppointment>()
                .eq(MedicalAppointment::getElderlyId, userId)
                .eq(status != null, MedicalAppointment::getStatus, status)
                .orderByDesc(MedicalAppointment::getCreateTime);
        List<MedicalAppointment> medicalAppointments = medicalAppointmentMapper.selectList(medicalWrapper);
        for (MedicalAppointment ma : medicalAppointments) {
            OrderDetailVO vo = new OrderDetailVO();
            vo.setId(ma.getId());
            vo.setOrderNo(ma.getOrderNo());
            vo.setOrderType("medical");
            vo.setStatus(ma.getStatus());
            vo.setAddress(ma.getAddress());
            vo.setRemark(ma.getSymptoms());
            vo.setServiceCode(ma.getServiceCode());
            vo.setCreateTime(ma.getCreateTime());
            vo.setAppointmentDate(ma.getAppointmentDate() != null ? ma.getAppointmentDate().toString() : null);
            vo.setAppointmentTime(ma.getAppointmentDate() + " 排队号:" + ma.getQueueNumber());
            
            MedicalDoctor doctor = medicalDoctorMapper.selectById(ma.getDoctorId());
            vo.setServiceName(doctor != null ? "医疗-" + doctor.getName() + "医生" : "医疗巡诊");
            if (doctor != null) {
                vo.setWorkerName(doctor.getName());
            }
            
            vo.setStatusText(getMedicalStatusText(ma.getStatus()));
            allOrders.add(vo);
        }
        
        // 按创建时间倒序排序
        allOrders.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });
        
        return Result.success(allOrders);
    }

    /**
     * 获取代办订单列表（子女端）- 整合所有类型订单
     */
    @GetMapping("/proxy")
    public Result<List<OrderDetailVO>> proxyOrders(@RequestAttribute Long userId) {
        List<OrderDetailVO> allOrders = new ArrayList<>();
        
        // 1. 查询通用服务订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getBookerId, userId)
                .ne(Order::getElderlyId, userId)
                .orderByDesc(Order::getCreateTime);
        List<Order> orders = orderService.list(wrapper);
        allOrders.addAll(orders.stream().map(this::convertToDetail).collect(Collectors.toList()));
        
        // 2. 查询保洁订单
        List<CleaningOrder> cleaningOrders = cleaningOrderMapper.selectList(
                new LambdaQueryWrapper<CleaningOrder>()
                        .eq(CleaningOrder::getBookerId, userId)
                        .ne(CleaningOrder::getElderlyId, userId)
                        .orderByDesc(CleaningOrder::getCreateTime));
        for (CleaningOrder co : cleaningOrders) {
            OrderDetailVO vo = new OrderDetailVO();
            vo.setId(co.getId());
            vo.setOrderNo(co.getOrderNo());
            vo.setOrderType("cleaning");
            vo.setStatus(co.getStatus());
            vo.setAmount(BigDecimal.valueOf(co.getAmount()));
            vo.setAddress(co.getAddress());
            vo.setRemark(co.getRemark());
            vo.setServiceCode(co.getServiceCode());
            vo.setCreateTime(co.getCreateTime());
            vo.setAppointmentTime(co.getServiceDate() + " " + co.getStartTime());
            vo.setAppointmentDate(co.getServiceDate() != null ? co.getServiceDate().toString() : null);
            
            // 获取服务名称
            CleaningService cs = cleaningServiceMapper.selectById(co.getServiceId());
            vo.setServiceName(cs != null ? "保洁-" + cs.getName() : "保洁服务");
            
            // 获取老人信息
            User elderly = userService.getById(co.getElderlyId());
            if (elderly != null) {
                vo.setElderlyName(elderly.getName());
                vo.setElderlyPhone(elderly.getPhone());
            }
            
            // 获取服务人员信息
            if (co.getWorkerId() != null) {
                User worker = userService.getById(co.getWorkerId());
                if (worker != null) {
                    vo.setWorkerName(worker.getName());
                    vo.setWorkerPhone(worker.getPhone());
                }
            }
            
            vo.setStatusText(getCleaningStatusText(co.getStatus()));
            allOrders.add(vo);
        }
        
        // 3. 查询餐饮订单
        List<MealOrder> mealOrders = mealOrderMapper.selectList(
                new LambdaQueryWrapper<MealOrder>()
                        .eq(MealOrder::getBookerId, userId)
                        .ne(MealOrder::getElderlyId, userId)
                        .orderByDesc(MealOrder::getCreateTime));
        for (MealOrder mo : mealOrders) {
            OrderDetailVO vo = new OrderDetailVO();
            vo.setId(mo.getId());
            vo.setOrderNo(mo.getOrderNo());
            vo.setOrderType("meal");
            vo.setStatus(mo.getStatus());
            vo.setAmount(BigDecimal.valueOf(mo.getAmount()));
            vo.setAddress(mo.getAddress());
            vo.setRemark(mo.getRemark());
            vo.setServiceCode(mo.getServiceCode());
            vo.setCreateTime(mo.getCreateTime());
            vo.setAppointmentDate(mo.getDishDate() != null ? mo.getDishDate().toString() : null);
            vo.setAppointmentTime(mo.getDishDate() + " " + getMealTypeText(mo.getMealType()));
            
            // 获取菜品名称
            MealDailyDish dish = mealDailyDishMapper.selectById(mo.getDishId());
            vo.setServiceName(dish != null ? "餐饮-" + dish.getDishName() : "餐饮配送");
            
            // 获取老人信息
            User elderly = userService.getById(mo.getElderlyId());
            if (elderly != null) {
                vo.setElderlyName(elderly.getName());
                vo.setElderlyPhone(elderly.getPhone());
            }
            
            // 获取服务人员信息
            if (mo.getWorkerId() != null) {
                User worker = userService.getById(mo.getWorkerId());
                if (worker != null) {
                    vo.setWorkerName(worker.getName());
                    vo.setWorkerPhone(worker.getPhone());
                }
            }
            
            vo.setStatusText(getMealStatusText(mo.getStatus()));
            allOrders.add(vo);
        }
        
        // 4. 查询医疗预约
        List<MedicalAppointment> medicalAppointments = medicalAppointmentMapper.selectList(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getBookerId, userId)
                        .ne(MedicalAppointment::getElderlyId, userId)
                        .orderByDesc(MedicalAppointment::getCreateTime));
        for (MedicalAppointment ma : medicalAppointments) {
            OrderDetailVO vo = new OrderDetailVO();
            vo.setId(ma.getId());
            vo.setOrderNo(ma.getOrderNo());
            vo.setOrderType("medical");
            vo.setStatus(ma.getStatus());
            vo.setAddress(ma.getAddress());
            vo.setRemark(ma.getRemark());
            vo.setServiceCode(ma.getServiceCode());
            vo.setCreateTime(ma.getCreateTime());
            vo.setAppointmentDate(ma.getAppointmentDate() != null ? ma.getAppointmentDate().toString() : null);
            vo.setAppointmentTime(ma.getAppointmentDate() + " 排队号:" + ma.getQueueNumber());
            
            // 获取医生信息
            MedicalDoctor doctor = medicalDoctorMapper.selectById(ma.getDoctorId());
            vo.setServiceName(doctor != null ? "医疗-" + doctor.getName() + "医生" : "医疗巡诊");
            if (doctor != null) {
                vo.setWorkerName(doctor.getName());
            }
            
            // 获取老人信息
            User elderly = userService.getById(ma.getElderlyId());
            if (elderly != null) {
                vo.setElderlyName(elderly.getName());
                vo.setElderlyPhone(elderly.getPhone());
            }
            
            vo.setStatusText(getMedicalStatusText(ma.getStatus()));
            allOrders.add(vo);
        }
        
        // 按创建时间倒序排序
        allOrders.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });
        
        return Result.success(allOrders);
    }
    
    private String getCleaningStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待服务";
            case 1: return "服务中";
            case 2: return "待确认";
            case 3: return "已完成";
            case 4: return "已取消";
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
            case 0: return "待巡诊";
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

    /**
     * 获取代办订单列表（子女端）- 按类型查询
     */
    @GetMapping("/proxy-by-type")
    public Result<List<OrderDetailVO>> proxyOrdersByType(
            @RequestAttribute Long userId,
            @RequestParam String type,
            @RequestParam(required = false) Integer status) {
        
        List<OrderDetailVO> orders = new ArrayList<>();
        
        switch (type) {
            case "cleaning":
                LambdaQueryWrapper<CleaningOrder> cleaningWrapper = new LambdaQueryWrapper<CleaningOrder>()
                        .eq(CleaningOrder::getBookerId, userId)
                        .ne(CleaningOrder::getElderlyId, userId)
                        .eq(status != null, CleaningOrder::getStatus, status)
                        .orderByDesc(CleaningOrder::getCreateTime);
                List<CleaningOrder> cleaningOrders = cleaningOrderMapper.selectList(cleaningWrapper);
                for (CleaningOrder co : cleaningOrders) {
                    OrderDetailVO vo = new OrderDetailVO();
                    vo.setId(co.getId());
                    vo.setOrderNo(co.getOrderNo());
                    vo.setOrderType("cleaning");
                    vo.setStatus(co.getStatus());
                    vo.setAmount(co.getAmount() != null ? BigDecimal.valueOf(co.getAmount()) : null);
                    vo.setAddress(co.getAddress());
                    vo.setRemark(co.getRemark());
                    vo.setServiceCode(co.getServiceCode());
                    vo.setCreateTime(co.getCreateTime());
                    vo.setAppointmentTime(co.getServiceDate() + " " + co.getStartTime() + "-" + co.getEndTime());
                    
                    CleaningService cs = cleaningServiceMapper.selectById(co.getServiceId());
                    vo.setServiceName(cs != null ? cs.getName() : "保洁服务");
                    
                    User elderly = userService.getById(co.getElderlyId());
                    if (elderly != null) {
                        vo.setElderlyName(elderly.getName());
                        vo.setElderlyPhone(elderly.getPhone());
                    }
                    
                    if (co.getWorkerId() != null) {
                        User worker = userService.getById(co.getWorkerId());
                        if (worker != null) {
                            vo.setWorkerName(worker.getName());
                        }
                    }
                    
                    vo.setStatusText(getCleaningStatusText(co.getStatus()));
                    orders.add(vo);
                }
                break;
                
            case "meal":
                LambdaQueryWrapper<MealOrder> mealWrapper = new LambdaQueryWrapper<MealOrder>()
                        .eq(MealOrder::getBookerId, userId)
                        .ne(MealOrder::getElderlyId, userId)
                        .eq(status != null, MealOrder::getStatus, status)
                        .orderByDesc(MealOrder::getCreateTime);
                List<MealOrder> mealOrders = mealOrderMapper.selectList(mealWrapper);
                for (MealOrder mo : mealOrders) {
                    OrderDetailVO vo = new OrderDetailVO();
                    vo.setId(mo.getId());
                    vo.setOrderNo(mo.getOrderNo());
                    vo.setOrderType("meal");
                    vo.setStatus(mo.getStatus());
                    vo.setAmount(mo.getAmount() != null ? BigDecimal.valueOf(mo.getAmount()) : null);
                    vo.setAddress(mo.getAddress());
                    vo.setRemark(mo.getRemark());
                    vo.setServiceCode(mo.getServiceCode());
                    vo.setCreateTime(mo.getCreateTime());
                    vo.setAppointmentTime(mo.getDishDate() + " " + getMealTypeText(mo.getMealType()));
                    
                    MealDailyDish dish = mealDailyDishMapper.selectById(mo.getDishId());
                    vo.setServiceName(dish != null ? dish.getDishName() : "餐饮配送");
                    
                    User elderly = userService.getById(mo.getElderlyId());
                    if (elderly != null) {
                        vo.setElderlyName(elderly.getName());
                        vo.setElderlyPhone(elderly.getPhone());
                    }
                    
                    if (mo.getWorkerId() != null) {
                        User worker = userService.getById(mo.getWorkerId());
                        if (worker != null) {
                            vo.setWorkerName(worker.getName());
                        }
                    }
                    
                    vo.setStatusText(getMealStatusText(mo.getStatus()));
                    orders.add(vo);
                }
                break;
                
            case "medical":
                LambdaQueryWrapper<MedicalAppointment> medicalWrapper = new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getBookerId, userId)
                        .ne(MedicalAppointment::getElderlyId, userId)
                        .eq(status != null, MedicalAppointment::getStatus, status)
                        .orderByDesc(MedicalAppointment::getCreateTime);
                List<MedicalAppointment> medicalOrders = medicalAppointmentMapper.selectList(medicalWrapper);
                for (MedicalAppointment ma : medicalOrders) {
                    OrderDetailVO vo = new OrderDetailVO();
                    vo.setId(ma.getId());
                    vo.setOrderNo(ma.getOrderNo());
                    vo.setOrderType("medical");
                    vo.setStatus(ma.getStatus());
                    vo.setAmount(BigDecimal.ZERO);
                    vo.setAddress(ma.getAddress());
                    vo.setRemark(ma.getSymptoms());
                    vo.setServiceCode(ma.getServiceCode());
                    vo.setCreateTime(ma.getCreateTime());
                    vo.setAppointmentTime(ma.getAppointmentDate() + " 排队号:" + ma.getQueueNumber());
                    
                    MedicalDoctor doctor = medicalDoctorMapper.selectById(ma.getDoctorId());
                    vo.setServiceName(doctor != null ? doctor.getName() + "医生" : "医疗巡诊");
                    if (doctor != null) {
                        vo.setWorkerName(doctor.getName());
                    }
                    
                    User elderly = userService.getById(ma.getElderlyId());
                    if (elderly != null) {
                        vo.setElderlyName(elderly.getName());
                        vo.setElderlyPhone(elderly.getPhone());
                    }
                    
                    vo.setStatusText(getMedicalStatusText(ma.getStatus()));
                    orders.add(vo);
                }
                break;
        }
        
        return Result.success(orders);
    }

    /**
     * 获取服务人员的工单列表
     */
    @GetMapping("/worker")
    public Result<List<OrderDetailVO>> workerOrders(
            @RequestAttribute Long userId,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getWorkerId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        
        List<Order> orders = orderService.list(wrapper);
        return Result.success(orders.stream()
                .map(this::convertToDetail)
                .collect(Collectors.toList()));
    }

    /**
     * 获取老人的预约日程（子女端查看）- 整合所有类型订单
     */
    @GetMapping("/schedule")
    public Result<List<java.util.Map<String, Object>>> getSchedule(
            @RequestParam Long elderlyId,
            @RequestParam(required = false) Boolean includeCompleted) {
        
        List<java.util.Map<String, Object>> allSchedules = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // 1. 查询通用服务订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getElderlyId, elderlyId)
                .ne(Order::getStatus, 5);
        if (includeCompleted == null || !includeCompleted) {
            wrapper.ge(Order::getAppointmentDate, today);
        }
        List<Order> orders = orderService.list(wrapper);
        for (Order order : orders) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", order.getId());
            item.put("orderNo", order.getOrderNo());
            if (order.getAppointmentDate() != null) {
                item.put("day", order.getAppointmentDate().getDayOfMonth());
                item.put("month", order.getAppointmentDate().getMonthValue() + "月");
                item.put("appointmentDate", order.getAppointmentDate().toString());
                item.put("sortDate", order.getAppointmentDate().toString());
            }
            ServiceItem serviceItem = serviceItemService.getById(order.getServiceItemId());
            item.put("serviceName", serviceItem != null ? serviceItem.getName() : "服务预约");
            item.put("time", order.getAppointmentTime() != null ? order.getAppointmentTime().toLocalTime().toString().substring(0, 5) : "待定");
            item.put("status", order.getStatus());
            item.put("statusText", getStatusText(order.getStatus()));
            item.put("address", order.getAddress());
            item.put("isProxy", order.getBookerId() != null && !order.getBookerId().equals(order.getElderlyId()));
            allSchedules.add(item);
        }
        
        // 2. 查询保洁订单
        LambdaQueryWrapper<CleaningOrder> cleaningWrapper = new LambdaQueryWrapper<CleaningOrder>()
                .eq(CleaningOrder::getElderlyId, elderlyId)
                .ne(CleaningOrder::getStatus, 4);
        if (includeCompleted == null || !includeCompleted) {
            cleaningWrapper.ge(CleaningOrder::getServiceDate, today);
        }
        List<CleaningOrder> cleaningOrders = cleaningOrderMapper.selectList(cleaningWrapper);
        for (CleaningOrder co : cleaningOrders) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", co.getId());
            item.put("orderNo", co.getOrderNo());
            if (co.getServiceDate() != null) {
                item.put("day", co.getServiceDate().getDayOfMonth());
                item.put("month", co.getServiceDate().getMonthValue() + "月");
                item.put("appointmentDate", co.getServiceDate().toString());
                item.put("sortDate", co.getServiceDate().toString());
            }
            CleaningService cs = cleaningServiceMapper.selectById(co.getServiceId());
            item.put("serviceName", cs != null ? "保洁-" + cs.getName() : "保洁服务");
            item.put("time", co.getStartTime() != null ? co.getStartTime().toString().substring(0, 5) : "待定");
            item.put("status", co.getStatus());
            item.put("statusText", getCleaningStatusText(co.getStatus()));
            item.put("address", co.getAddress());
            item.put("isProxy", co.getBookerId() != null && !co.getBookerId().equals(co.getElderlyId()));
            allSchedules.add(item);
        }
        
        // 3. 查询餐饮订单
        LambdaQueryWrapper<MealOrder> mealWrapper = new LambdaQueryWrapper<MealOrder>()
                .eq(MealOrder::getElderlyId, elderlyId)
                .ne(MealOrder::getStatus, 3);
        if (includeCompleted == null || !includeCompleted) {
            mealWrapper.ge(MealOrder::getDishDate, today);
        }
        List<MealOrder> mealOrders = mealOrderMapper.selectList(mealWrapper);
        for (MealOrder mo : mealOrders) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", mo.getId());
            item.put("orderNo", mo.getOrderNo());
            if (mo.getDishDate() != null) {
                item.put("day", mo.getDishDate().getDayOfMonth());
                item.put("month", mo.getDishDate().getMonthValue() + "月");
                item.put("appointmentDate", mo.getDishDate().toString());
                item.put("sortDate", mo.getDishDate().toString());
            }
            MealDailyDish dish = mealDailyDishMapper.selectById(mo.getDishId());
            item.put("serviceName", dish != null ? "餐饮-" + dish.getDishName() : "餐饮配送");
            item.put("time", getMealTypeText(mo.getMealType()));
            item.put("status", mo.getStatus());
            item.put("statusText", getMealStatusText(mo.getStatus()));
            item.put("address", mo.getAddress());
            item.put("isProxy", mo.getBookerId() != null && !mo.getBookerId().equals(mo.getElderlyId()));
            allSchedules.add(item);
        }
        
        // 4. 查询医疗预约
        LambdaQueryWrapper<MedicalAppointment> medicalWrapper = new LambdaQueryWrapper<MedicalAppointment>()
                .eq(MedicalAppointment::getElderlyId, elderlyId)
                .ne(MedicalAppointment::getStatus, 3);
        if (includeCompleted == null || !includeCompleted) {
            medicalWrapper.ge(MedicalAppointment::getAppointmentDate, today);
        }
        List<MedicalAppointment> medicalAppointments = medicalAppointmentMapper.selectList(medicalWrapper);
        for (MedicalAppointment ma : medicalAppointments) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", ma.getId());
            item.put("orderNo", ma.getOrderNo());
            if (ma.getAppointmentDate() != null) {
                item.put("day", ma.getAppointmentDate().getDayOfMonth());
                item.put("month", ma.getAppointmentDate().getMonthValue() + "月");
                item.put("appointmentDate", ma.getAppointmentDate().toString());
                item.put("sortDate", ma.getAppointmentDate().toString());
            }
            MedicalDoctor doctor = medicalDoctorMapper.selectById(ma.getDoctorId());
            item.put("serviceName", doctor != null ? "医疗-" + doctor.getName() + "医生" : "医疗巡诊");
            item.put("time", "排队号:" + ma.getQueueNumber());
            item.put("status", ma.getStatus());
            item.put("statusText", getMedicalStatusText(ma.getStatus()));
            item.put("address", ma.getAddress());
            item.put("isProxy", ma.getBookerId() != null && !ma.getBookerId().equals(ma.getElderlyId()));
            allSchedules.add(item);
        }
        
        // 按日期排序
        allSchedules.sort((a, b) -> {
            String dateA = (String) a.get("sortDate");
            String dateB = (String) b.get("sortDate");
            if (dateA == null) return 1;
            if (dateB == null) return -1;
            return dateA.compareTo(dateB);
        });
        
        // 限制返回数量
        if (allSchedules.size() > 30) {
            allSchedules = allSchedules.subList(0, 30);
        }
        
        return Result.success(allSchedules);
    }

    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable Long id, @RequestAttribute Long userId) {
        return Result.success(orderService.cancelOrder(id, userId));
    }

    @PostMapping("/{id}/accept")
    public Result<Boolean> acceptOrder(@PathVariable Long id, @RequestAttribute Long userId) {
        return Result.success(orderService.acceptOrder(id, userId));
    }

    /**
     * 管理员分配订单给服务人员
     */
    @PostMapping("/{id}/assign")
    public Result<Boolean> assignOrder(@PathVariable Long id, @RequestBody java.util.Map<String, Long> body) {
        Long workerId = body.get("workerId");
        if (workerId == null) {
            throw new RuntimeException("请选择服务人员");
        }
        return Result.success(orderService.acceptOrder(id, workerId));
    }

    @PostMapping("/{id}/start")
    public Result<Boolean> startService(@PathVariable Long id, @RequestParam String serviceCode) {
        return Result.success(orderService.startService(id, serviceCode));
    }

    @PostMapping("/{id}/complete")
    public Result<Boolean> completeService(@PathVariable Long id, @RequestParam String evidence) {
        return Result.success(orderService.completeService(id, evidence));
    }

    /**
     * 老人确认服务完成
     */
    @PostMapping("/{id}/confirm")
    public Result<Boolean> confirmService(@PathVariable Long id, @RequestAttribute Long userId) {
        Order order = orderService.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!userId.equals(order.getElderlyId()) && !userId.equals(order.getBookerId())) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 3) {
            throw new RuntimeException("订单状态不正确");
        }
        order.setStatus(4);
        return Result.success(orderService.updateById(order));
    }

    @GetMapping("/page")
    public Result<Page<Order>> pageOrders(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        return Result.success(orderService.page(pageParam, wrapper));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return Result.success(convertToDetail(order));
    }

    /**
     * 获取可接单列表（服务人员端）
     */
    @GetMapping("/available")
    public Result<List<OrderDetailVO>> getAvailableOrders() {
        List<Order> orders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 0)
                .orderByAsc(Order::getAppointmentTime));
        
        return Result.success(orders.stream()
                .map(this::convertToDetail)
                .collect(Collectors.toList()));
    }

    /**
     * 转换为详情VO
     */
    private OrderDetailVO convertToDetail(Order order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setAmount(order.getAmount());
        vo.setAddress(order.getAddress());
        vo.setRemark(order.getRemark());
        vo.setServiceCode(order.getServiceCode());
        vo.setEvidence(order.getEvidence());
        vo.setAppointmentTime(order.getAppointmentTime() != null ? 
                order.getAppointmentTime().toString().replace("T", " ") : null);
        
        // 获取服务项目信息
        ServiceItem item = serviceItemService.getById(order.getServiceItemId());
        if (item != null) {
            vo.setServiceName(item.getName());
        }
        
        // 获取老人信息
        User elderly = userService.getById(order.getElderlyId());
        if (elderly != null) {
            vo.setElderlyName(elderly.getName());
            vo.setElderlyPhone(elderly.getPhone());
        }
        
        // 获取服务人员信息
        if (order.getWorkerId() != null) {
            User worker = userService.getById(order.getWorkerId());
            if (worker != null) {
                vo.setWorkerName(worker.getName());
                vo.setWorkerPhone(worker.getPhone());
            }
        }
        
        // 状态文本
        vo.setStatusText(getStatusText(order.getStatus()));
        
        return vo;
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待接单";
            case 1: return "已接单";
            case 2: return "服务中";
            case 3: return "待确认";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知";
        }
    }
}
