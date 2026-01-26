package com.elderly.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderly.entity.HealthRecord;
import com.elderly.mapper.HealthRecordMapper;
import com.elderly.service.HealthRecordService;
import org.springframework.stereotype.Service;

@Service
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements HealthRecordService {

    @Override
    public void saveHealthInfo(Long elderlyId, Long orderId, String symptoms, String allergies, String medications) {
        HealthRecord record = new HealthRecord();
        record.setElderlyId(elderlyId);
        record.setOrderId(orderId);
        record.setSymptoms(symptoms);
        record.setAllergies(allergies);
        record.setMedications(medications);
        save(record);
    }
}
