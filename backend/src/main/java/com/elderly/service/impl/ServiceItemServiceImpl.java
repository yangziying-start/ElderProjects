package com.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderly.dto.TimeSlotVO;
import com.elderly.entity.Order;
import com.elderly.entity.ServiceItem;
import com.elderly.entity.ServiceTimeSlot;
import com.elderly.mapper.OrderMapper;
import com.elderly.mapper.ServiceItemMapper;
import com.elderly.mapper.ServiceTimeSlotMapper;
import com.elderly.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl extends ServiceImpl<ServiceItemMapper, ServiceItem> implements ServiceItemService {

    private final OrderMapper orderMapper;
    private final ServiceTimeSlotMapper timeSlotMapper;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public int getAvailableCapacity(Long serviceItemId, LocalDate date) {
        ServiceItem item = getById(serviceItemId);
        if (item == null) {
            return 0;
        }
        
        // 统计该日期所有时间段的剩余容量总和
        List<TimeSlotVO> slots = getAvailableTimeSlots(serviceItemId, date);
        return slots.stream()
                .mapToInt(TimeSlotVO::getRemainingCapacity)
                .sum();
    }

    @Override
    public List<TimeSlotVO> getAvailableTimeSlots(Long serviceItemId, LocalDate date) {
        List<TimeSlotVO> result = new ArrayList<>();
        
        // 查询该服务的所有启用时间段
        List<ServiceTimeSlot> slots = timeSlotMapper.selectList(
                new LambdaQueryWrapper<ServiceTimeSlot>()
                        .eq(ServiceTimeSlot::getServiceItemId, serviceItemId)
                        .eq(ServiceTimeSlot::getStatus, 1)
                        .orderByAsc(ServiceTimeSlot::getStartTime));
        
        if (slots.isEmpty()) {
            // 如果没有配置时间段，返回默认时间段
            return getDefaultTimeSlots(serviceItemId, date);
        }
        
        for (ServiceTimeSlot slot : slots) {
            TimeSlotVO vo = new TimeSlotVO();
            vo.setId(slot.getId());
            vo.setStartTime(slot.getStartTime().format(TIME_FORMATTER));
            vo.setEndTime(slot.getEndTime().format(TIME_FORMATTER));
            vo.setCapacity(slot.getCapacity());
            vo.setDisplayText(vo.getStartTime() + "-" + vo.getEndTime());
            
            // 查询该时间段已预约数量
            long bookedCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .eq(Order::getServiceItemId, serviceItemId)
                    .eq(Order::getTimeSlotId, slot.getId())
                    .eq(Order::getAppointmentDate, date)
                    .ne(Order::getStatus, 5)); // 排除已取消
            
            vo.setBookedCount((int) bookedCount);
            vo.setRemainingCapacity(Math.max(0, slot.getCapacity() - (int) bookedCount));
            vo.setIsFull(vo.getRemainingCapacity() <= 0);
            
            result.add(vo);
        }
        
        return result;
    }

    @Override
    public boolean checkTimeSlotCapacity(Long serviceItemId, Long timeSlotId, LocalDate date) {
        ServiceTimeSlot slot = timeSlotMapper.selectById(timeSlotId);
        if (slot == null || !slot.getServiceItemId().equals(serviceItemId)) {
            return false;
        }
        
        long bookedCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getServiceItemId, serviceItemId)
                .eq(Order::getTimeSlotId, timeSlotId)
                .eq(Order::getAppointmentDate, date)
                .ne(Order::getStatus, 5));
        
        return bookedCount < slot.getCapacity();
    }
    
    /**
     * 获取默认时间段（当服务没有配置时间段时使用）
     */
    private List<TimeSlotVO> getDefaultTimeSlots(Long serviceItemId, LocalDate date) {
        ServiceItem item = getById(serviceItemId);
        if (item == null) {
            return new ArrayList<>();
        }
        
        List<TimeSlotVO> result = new ArrayList<>();
        String[][] defaultSlots = {
                {"08:00", "10:00"},
                {"10:00", "12:00"},
                {"14:00", "16:00"},
                {"16:00", "18:00"}
        };
        
        int capacityPerSlot = item.getDailyCapacity() != null ? item.getDailyCapacity() / 4 : 2;
        
        for (int i = 0; i < defaultSlots.length; i++) {
            TimeSlotVO vo = new TimeSlotVO();
            vo.setId((long) -(i + 1)); // 负数ID表示默认时间段
            vo.setStartTime(defaultSlots[i][0]);
            vo.setEndTime(defaultSlots[i][1]);
            vo.setCapacity(capacityPerSlot);
            vo.setDisplayText(vo.getStartTime() + "-" + vo.getEndTime());
            vo.setBookedCount(0);
            vo.setRemainingCapacity(capacityPerSlot);
            vo.setIsFull(false);
            result.add(vo);
        }
        
        return result;
    }
}
