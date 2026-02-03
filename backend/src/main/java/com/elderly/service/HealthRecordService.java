package com.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderly.entity.HealthRecord;

public interface HealthRecordService extends IService<HealthRecord> {
    /**
     * 保存医疗预约的健康信息
     */
    void saveHealthInfo(Long elderlyId, Long orderId, String symptoms, String allergies, String medications);
}
