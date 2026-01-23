package com.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderly.dto.BookingRequest;
import com.elderly.entity.Order;
import com.elderly.entity.ServiceCategory;
import com.elderly.entity.ServiceItem;
import com.elderly.entity.ServiceTimeSlot;
import com.elderly.mapper.OrderMapper;
import com.elderly.mapper.ServiceCategoryMapper;
import com.elderly.mapper.ServiceTimeSlotMapper;
import com.elderly.service.HealthRecordService;
import com.elderly.service.OrderService;
import com.elderly.service.ServiceItemService;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final ServiceItemService serviceItemService;
    private final ServiceTimeSlotMapper timeSlotMapper;
    private final ServiceCategoryMapper categoryMapper;
    private final HealthRecordService healthRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(BookingRequest request, Long userId) {
        // 1. 再次检查时间段容量
        if (request.getTimeSlotId() != null && request.getTimeSlotId() > 0) {
            boolean hasCapacity = serviceItemService.checkTimeSlotCapacity(
                    request.getServiceItemId(), 
                    request.getTimeSlotId(), 
                    request.getAppointmentDate());
            if (!hasCapacity) {
                throw new RuntimeException("该时间段已约满，请选择其他时间段");
            }
        }
        
        // 2. 获取服务项目信息
        ServiceItem serviceItem = serviceItemService.getById(request.getServiceItemId());
        if (serviceItem == null) {
            throw new RuntimeException("服务项目不存在");
        }
        
        // 3. 创建订单
        Order order = new Order();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setServiceItemId(request.getServiceItemId());
        order.setAppointmentDate(request.getAppointmentDate());
        order.setTimeSlotId(request.getTimeSlotId());
        order.setAddress(request.getAddress());
        order.setRemark(request.getRemark());
        order.setAmount(serviceItem.getPrice());
        order.setStatus(0);
        
        // 设置老人ID（自己预约或子女代预约）
        if (request.getElderlyId() != null) {
            order.setElderlyId(request.getElderlyId());
            order.setBookerId(userId);
        } else {
            order.setElderlyId(userId);
            order.setBookerId(userId);
        }
        
        // 设置预约时间（日期+时间段开始时间）
        if (request.getTimeSlotId() != null && request.getTimeSlotId() > 0) {
            ServiceTimeSlot slot = timeSlotMapper.selectById(request.getTimeSlotId());
            if (slot != null) {
                order.setAppointmentTime(request.getAppointmentDate().atTime(slot.getStartTime()));
            }
        }
        
        // 生成服务码（如果需要）
        if (serviceItem.getNeedServiceCode() != null && serviceItem.getNeedServiceCode() == 1) {
            order.setServiceCode(RandomUtil.randomNumbers(6));
        }
        
        save(order);
        
        // 4. 如果是医疗预约，保存健康信息到健康档案
        if (request.getHealthInfo() != null) {
            ServiceCategory category = categoryMapper.selectById(serviceItem.getCategoryId());
            if (category != null && category.getIsMedical() != null && category.getIsMedical() == 1) {
                healthRecordService.saveHealthInfo(
                        order.getElderlyId(),
                        order.getId(),
                        request.getHealthInfo().getSymptoms(),
                        request.getHealthInfo().getAllergies(),
                        request.getHealthInfo().getMedications());
            }
        }
        
        return order.getId();
    }

    @Override
    public Long createOrder(Order order) {
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setStatus(0);
        save(order);
        return order.getId();
    }

    @Override
    public boolean cancelOrder(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null || order.getStatus() > 1) {
            return false;
        }
        // 检查是否在3分钟内
        if (order.getCreateTime().plusMinutes(3).isBefore(LocalDateTime.now())) {
            return false;
        }
        order.setStatus(5);
        return updateById(order);
    }

    @Override
    public boolean acceptOrder(Long orderId, Long workerId) {
        Order order = getById(orderId);
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        order.setWorkerId(workerId);
        order.setStatus(1);
        return updateById(order);
    }

    @Override
    public boolean startService(Long orderId, String serviceCode) {
        Order order = getById(orderId);
        if (order == null || order.getStatus() != 1) {
            return false;
        }
        if (order.getServiceCode() != null && !serviceCode.equals(order.getServiceCode())) {
            return false;
        }
        order.setStatus(2);
        return updateById(order);
    }

    @Override
    public boolean completeService(Long orderId, String evidence) {
        Order order = getById(orderId);
        if (order == null || order.getStatus() != 2) {
            return false;
        }
        order.setEvidence(evidence);
        order.setStatus(3);
        return updateById(order);
    }
}
