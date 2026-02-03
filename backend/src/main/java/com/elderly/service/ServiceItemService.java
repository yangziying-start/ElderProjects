package com.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderly.dto.TimeSlotVO;
import com.elderly.entity.ServiceItem;
import java.time.LocalDate;
import java.util.List;

public interface ServiceItemService extends IService<ServiceItem> {
    int getAvailableCapacity(Long serviceItemId, LocalDate date);
    
    /**
     * 获取服务项目在指定日期的可预约时间段列表
     */
    List<TimeSlotVO> getAvailableTimeSlots(Long serviceItemId, LocalDate date);
    
    /**
     * 检查指定时间段是否还有剩余容量
     */
    boolean checkTimeSlotCapacity(Long serviceItemId, Long timeSlotId, LocalDate date);
}
