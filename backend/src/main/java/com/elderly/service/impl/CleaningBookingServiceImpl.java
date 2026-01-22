package com.elderly.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.dto.CleaningBookingRequest;
import com.elderly.dto.CleaningWorkerVO;
import com.elderly.entity.CleaningOrder;
import com.elderly.entity.CleaningService;
import com.elderly.entity.CleaningWorkerSchedule;
import com.elderly.entity.User;
import com.elderly.mapper.CleaningOrderMapper;
import com.elderly.mapper.CleaningServiceMapper;
import com.elderly.mapper.CleaningWorkerScheduleMapper;
import com.elderly.mapper.UserMapper;
import com.elderly.service.CleaningBookingService;
import com.elderly.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CleaningBookingServiceImpl implements CleaningBookingService {

    private final CleaningServiceMapper cleaningServiceMapper;
    private final CleaningWorkerScheduleMapper scheduleMapper;
    private final CleaningOrderMapper cleaningOrderMapper;
    private final UserMapper userMapper;
    private final PointsService pointsService;

    @Override
    public List<CleaningService> getServiceList() {
        return cleaningServiceMapper.selectList(
                new LambdaQueryWrapper<CleaningService>()
                        .eq(CleaningService::getStatus, 1)
                        .orderByAsc(CleaningService::getSort)
        );
    }

    @Override
    public List<CleaningWorkerVO> getAvailableWorkers(LocalDate date) {
        // 获取所有保洁员（userType=3 且 workerType=2）
        List<User> workers = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUserType, 3)
                        .eq(User::getWorkerType, 2)  // 2-保洁员
                        .eq(User::getStatus, 1));
        
        List<CleaningWorkerVO> result = new ArrayList<>();
        
        for (User worker : workers) {
            CleaningWorkerVO vo = new CleaningWorkerVO();
            vo.setWorkerId(worker.getId());
            vo.setWorkerName(worker.getName());
            vo.setAvatar(worker.getAvatar());
            
            // 查询该服务人员当天的排班
            CleaningWorkerSchedule schedule = scheduleMapper.selectByWorkerAndDate(worker.getId(), date);
            if (schedule != null && schedule.getStatus() == 1) {
                vo.setScheduleStartTime(schedule.getStartTime());
                vo.setScheduleEndTime(schedule.getEndTime());
                
                // 获取已预约时段
                List<CleaningOrder> bookedOrders = cleaningOrderMapper.selectWorkerBookedSlots(worker.getId(), date);
                List<CleaningWorkerVO.TimeSlot> bookedSlots = new ArrayList<>();
                for (CleaningOrder order : bookedOrders) {
                    bookedSlots.add(new CleaningWorkerVO.TimeSlot(order.getStartTime(), order.getEndTime()));
                }
                vo.setBookedSlots(bookedSlots);
                
                // 计算可预约时段
                vo.setAvailableSlots(calculateAvailableSlots(schedule.getStartTime(), schedule.getEndTime(), bookedSlots));
            } else {
                // 没有排班，设置默认工作时间
                vo.setScheduleStartTime(LocalTime.of(8, 0));
                vo.setScheduleEndTime(LocalTime.of(18, 0));
                vo.setBookedSlots(new ArrayList<>());
                vo.setAvailableSlots(new ArrayList<>());
            }
            
            result.add(vo);
        }
        
        return result;
    }

    private List<CleaningWorkerVO.TimeSlot> calculateAvailableSlots(LocalTime start, LocalTime end, 
                                                                     List<CleaningWorkerVO.TimeSlot> bookedSlots) {
        List<CleaningWorkerVO.TimeSlot> available = new ArrayList<>();
        LocalTime current = start;
        
        // 按开始时间排序已预约时段
        bookedSlots.sort(Comparator.comparing(CleaningWorkerVO.TimeSlot::getStartTime));
        
        for (CleaningWorkerVO.TimeSlot booked : bookedSlots) {
            if (current.isBefore(booked.getStartTime())) {
                available.add(new CleaningWorkerVO.TimeSlot(current, booked.getStartTime()));
            }
            current = booked.getEndTime();
        }
        
        if (current.isBefore(end)) {
            available.add(new CleaningWorkerVO.TimeSlot(current, end));
        }
        
        return available;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(CleaningBookingRequest request, Long userId) {
        // 验证时长(最小30分钟，30分钟为单位)
        if (request.getDuration() < 30 || request.getDuration() % 30 != 0) {
            throw new RuntimeException("服务时长必须为30分钟的整数倍，最少30分钟");
        }
        
        // 验证预约时间不能是过去的时间
        LocalDateTime serviceStartDateTime = request.getServiceDate().atTime(request.getStartTime());
        if (serviceStartDateTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("预约时间不能早于当前时间");
        }
        
        // 检查服务人员是否存在且是保洁员
        User worker = userMapper.selectById(request.getWorkerId());
        if (worker == null || worker.getUserType() != 3 || worker.getStatus() != 1) {
            throw new RuntimeException("服务人员不存在或不可用");
        }
        if (worker.getWorkerType() == null || worker.getWorkerType() != 2) {
            throw new RuntimeException("该服务人员不是保洁员");
        }
        
        // 检查保洁员排班，如果没有排班则自动创建
        CleaningWorkerSchedule schedule = scheduleMapper.selectByWorkerAndDate(
                request.getWorkerId(), request.getServiceDate());
        if (schedule == null) {
            // 自动创建排班
            schedule = new CleaningWorkerSchedule();
            schedule.setWorkerId(request.getWorkerId());
            schedule.setScheduleDate(request.getServiceDate());
            schedule.setStartTime(LocalTime.of(8, 0));
            schedule.setEndTime(LocalTime.of(18, 0));
            schedule.setStatus(1);
            scheduleMapper.insert(schedule);
        } else if (schedule.getStatus() != 1) {
            throw new RuntimeException("该服务人员当日休息");
        }
        
        // 计算结束时间
        LocalTime endTime = request.getStartTime().plusMinutes(request.getDuration());
        
        // 检查时间是否在排班范围内
        if (request.getStartTime().isBefore(schedule.getStartTime()) || 
            endTime.isAfter(schedule.getEndTime())) {
            throw new RuntimeException("预约时间超出服务人员工作时间");
        }
        
        // 检查时间段是否冲突
        List<CleaningOrder> bookedOrders = cleaningOrderMapper.selectWorkerBookedSlots(
                request.getWorkerId(), request.getServiceDate());
        for (CleaningOrder booked : bookedOrders) {
            if (isTimeOverlap(request.getStartTime(), endTime, booked.getStartTime(), booked.getEndTime())) {
                throw new RuntimeException("该时间段已被预约");
            }
        }
        
        // 获取服务项目计算价格
        CleaningService service = cleaningServiceMapper.selectById(request.getServiceId());
        if (service == null || service.getStatus() != 1) {
            throw new RuntimeException("服务项目不存在或已下架");
        }
        
        int units = request.getDuration() / 30;
        int totalAmount = service.getPricePer30min() * units;
        
        // 扣除积分
        Long elderlyId = request.getElderlyId() != null ? request.getElderlyId() : userId;
        pointsService.consume(elderlyId, totalAmount, "cleaning", null, "保洁预约-" + service.getName());
        
        // 创建订单
        CleaningOrder order = new CleaningOrder();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setElderlyId(elderlyId);
        order.setBookerId(userId);
        order.setWorkerId(request.getWorkerId());
        order.setServiceId(request.getServiceId());
        order.setServiceDate(request.getServiceDate());
        order.setStartTime(request.getStartTime());
        order.setEndTime(endTime);
        order.setDuration(request.getDuration());
        order.setAmount(totalAmount);
        order.setRemark(request.getRemark());
        order.setStatus(0);
        
        // 设置地址：优先使用传入的地址，否则获取老人的默认地址
        String address = request.getAddress();
        if (address == null || address.isEmpty()) {
            User elderly = userMapper.selectById(elderlyId);
            if (elderly != null && elderly.getAddress() != null) {
                address = elderly.getAddress();
            }
        }
        order.setAddress(address);
        
        // 生成6位服务码
        order.setServiceCode(String.valueOf((int) ((Math.random() * 900000) + 100000)));
        
        cleaningOrderMapper.insert(order);
        return order.getId();
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancelOrder(Long orderId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        CleaningOrder order = cleaningOrderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getElderlyId().equals(userId) && !order.getBookerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() >= 3) {
            throw new RuntimeException("订单状态不允许取消");
        }
        
        LocalDateTime serviceStart = order.getServiceDate().atTime(order.getStartTime());
        LocalDateTime now = LocalDateTime.now();
        long minutesBeforeService = java.time.Duration.between(now, serviceStart).toMinutes();
        
        int deductionRate = 0;
        String reason;
        if (minutesBeforeService > 30) {
            // 服务开始30分钟前：免费取消
            deductionRate = 0;
            reason = "免费取消";
        } else {
            // 服务开始30分钟内：扣5%
            deductionRate = 5;
            reason = "服务开始前30分钟内取消，扣除5%积分";
        }
        
        int deduction = order.getAmount() * deductionRate / 100;
        int refund = order.getAmount() - deduction;
        
        // 退还积分
        if (refund > 0) {
            pointsService.refund(order.getElderlyId(), refund, "cleaning", orderId, "保洁订单取消退款");
        }
        
        // 更新订单状态
        order.setStatus(4);
        order.setCancelTime(now);
        order.setCancelDeduction(deduction);
        order.setRefundAmount(refund);
        cleaningOrderMapper.updateById(order);
        
        result.put("deduction", deduction);
        result.put("refund", refund);
        result.put("reason", reason);
        return result;
    }

    @Override
    public List<CleaningOrder> getUserOrders(Long userId, Integer status) {
        LambdaQueryWrapper<CleaningOrder> wrapper = new LambdaQueryWrapper<CleaningOrder>()
                .eq(CleaningOrder::getElderlyId, userId)
                .eq(status != null, CleaningOrder::getStatus, status)
                .orderByDesc(CleaningOrder::getCreateTime);
        
        List<CleaningOrder> orders = cleaningOrderMapper.selectList(wrapper);
        for (CleaningOrder order : orders) {
            order.setService(cleaningServiceMapper.selectById(order.getServiceId()));
            order.setWorker(userMapper.selectById(order.getWorkerId()));
        }
        return orders;
    }

    @Override
    public boolean startService(Long orderId, String serviceCode) {
        CleaningOrder order = cleaningOrderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        if (!serviceCode.equals(order.getServiceCode())) {
            throw new RuntimeException("服务码错误");
        }
        order.setStatus(1);
        order.setActualStartTime(LocalDateTime.now());
        return cleaningOrderMapper.updateById(order) > 0;
    }

    @Override
    public boolean completeService(Long orderId, String evidence) {
        CleaningOrder order = cleaningOrderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) {
            return false;
        }
        order.setStatus(2);
        order.setActualEndTime(LocalDateTime.now());
        order.setEvidence(evidence);
        return cleaningOrderMapper.updateById(order) > 0;
    }
}
