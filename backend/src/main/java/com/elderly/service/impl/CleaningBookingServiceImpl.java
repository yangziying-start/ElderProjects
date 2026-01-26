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
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        boolean isToday = date.equals(today);
        
        for (User worker : workers) {
            // 查询该服务人员当天的排班
            CleaningWorkerSchedule schedule = scheduleMapper.selectByWorkerAndDate(worker.getId(), date);
            
            // 只返回当天有排班的保洁员
            if (schedule == null || schedule.getStatus() != 1) {
                continue;
            }
            
            CleaningWorkerVO vo = new CleaningWorkerVO();
            vo.setWorkerId(worker.getId());
            vo.setWorkerName(worker.getName());
            vo.setAvatar(worker.getAvatar());
            vo.setHasSchedule(true);
            
            vo.setScheduleStartTime(schedule.getStartTime());
            vo.setScheduleEndTime(schedule.getEndTime());
            
            // 判断是否已下班（仅当天需要判断）
            if (isToday && currentTime.isAfter(schedule.getEndTime())) {
                vo.setOffDuty(true);
            } else {
                vo.setOffDuty(false);
            }
            
            // 获取已预约时段
            List<CleaningOrder> bookedOrders = cleaningOrderMapper.selectWorkerBookedSlots(worker.getId(), date);
            List<CleaningWorkerVO.TimeSlot> bookedSlots = new ArrayList<>();
            for (CleaningOrder order : bookedOrders) {
                bookedSlots.add(new CleaningWorkerVO.TimeSlot(order.getStartTime(), order.getEndTime()));
            }
            vo.setBookedSlots(bookedSlots);
            
            // 计算可预约时段（如果是今天，需要排除已过去的时间）
            LocalTime effectiveStartTime = schedule.getStartTime();
            if (isToday && currentTime.isAfter(schedule.getStartTime())) {
                // 向上取整到下一个30分钟
                int minute = currentTime.getMinute();
                int roundedMinute = ((minute / 30) + 1) * 30;
                if (roundedMinute >= 60) {
                    effectiveStartTime = currentTime.plusHours(1).withMinute(roundedMinute - 60).withSecond(0);
                } else {
                    effectiveStartTime = currentTime.withMinute(roundedMinute).withSecond(0);
                }
            }
            
            if (effectiveStartTime.isBefore(schedule.getEndTime())) {
                vo.setAvailableSlots(calculateAvailableSlots(effectiveStartTime, schedule.getEndTime(), bookedSlots));
            } else {
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
        
        // 检查保洁员排班
        CleaningWorkerSchedule schedule = scheduleMapper.selectByWorkerAndDate(
                request.getWorkerId(), request.getServiceDate());
        if (schedule == null) {
            throw new RuntimeException("该保洁员当日未排班，请选择其他保洁员");
        }
        if (schedule.getStatus() != 1) {
            throw new RuntimeException("该保洁员当日休息，请选择其他保洁员");
        }
        
        // 计算结束时间
        LocalTime endTime = request.getStartTime().plusMinutes(request.getDuration());
        
        // 检查时间是否在排班范围内
        if (request.getStartTime().isBefore(schedule.getStartTime())) {
            throw new RuntimeException("预约开始时间不能早于保洁员上班时间（" + schedule.getStartTime() + "）");
        }
        if (endTime.isAfter(schedule.getEndTime())) {
            throw new RuntimeException("预约结束时间不能晚于保洁员下班时间（" + schedule.getEndTime() + "）");
        }
        
        // 如果是当天预约，检查是否已过保洁员下班时间
        if (request.getServiceDate().equals(LocalDate.now())) {
            LocalTime currentTime = LocalTime.now();
            if (currentTime.isAfter(schedule.getEndTime())) {
                throw new RuntimeException("该保洁员今日已下班，请选择其他日期或保洁员");
            }
            if (request.getStartTime().isBefore(currentTime)) {
                throw new RuntimeException("预约时间不能早于当前时间");
            }
        }
        
        // 检查时间段是否冲突
        List<CleaningOrder> bookedOrders = cleaningOrderMapper.selectWorkerBookedSlots(
                request.getWorkerId(), request.getServiceDate());
        for (CleaningOrder booked : bookedOrders) {
            if (isTimeOverlap(request.getStartTime(), endTime, booked.getStartTime(), booked.getEndTime())) {
                throw new RuntimeException("该时间段（" + booked.getStartTime() + "-" + booked.getEndTime() + "）已被预约，请选择其他时间");
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

    @Override
    public boolean confirmService(Long orderId, Long userId) {
        CleaningOrder order = cleaningOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getElderlyId().equals(userId) && !order.getBookerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确，只有待确认状态才能确认完成");
        }
        order.setStatus(3);
        return cleaningOrderMapper.updateById(order) > 0;
    }

    @Override
    public boolean disputeService(Long orderId, Long userId, String reason) {
        CleaningOrder order = cleaningOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getElderlyId().equals(userId) && !order.getBookerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确，只有待确认状态才能提交争议");
        }
        // 状态改为5-争议中，并记录争议原因
        order.setStatus(5);
        order.setRemark((order.getRemark() != null ? order.getRemark() + " | " : "") + "争议原因: " + reason);
        return cleaningOrderMapper.updateById(order) > 0;
    }
    
    @Override
    public boolean resolveDispute(Long orderId) {
        CleaningOrder order = cleaningOrderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 5) {
            throw new RuntimeException("订单状态不正确，只有争议中状态才能处理");
        }
        order.setStatus(3); // 已完成
        order.setRemark((order.getRemark() != null ? order.getRemark() + " | " : "") + "争议已处理");
        return cleaningOrderMapper.updateById(order) > 0;
    }
}
