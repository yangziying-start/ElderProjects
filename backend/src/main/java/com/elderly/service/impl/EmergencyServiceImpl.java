package com.elderly.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderly.entity.EmergencyCall;
import com.elderly.entity.User;
import com.elderly.mapper.EmergencyCallMapper;
import com.elderly.service.EmergencyService;
import com.elderly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmergencyServiceImpl extends ServiceImpl<EmergencyCallMapper, EmergencyCall> implements EmergencyService {

    private final UserService userService;

    @Override
    public Long triggerCall(Long elderlyId, Integer eventType) {
        User elderly = userService.getById(elderlyId);
        
        EmergencyCall call = new EmergencyCall();
        call.setElderlyId(elderlyId);
        call.setElderlyName(elderly.getName());
        call.setPhone(elderly.getPhone());
        call.setAddress(elderly.getAddress());
        call.setEventType(eventType);
        call.setTriggerTime(LocalDateTime.now());
        call.setStatus(0);
        call.setEscalated(0);
        save(call);
        
        return call.getId();
    }

    @Override
    public boolean respond(Long callId, Long responderId) {
        EmergencyCall call = getById(callId);
        if (call == null || call.getStatus() != 0) {
            return false;
        }
        call.setResponderId(responderId);
        call.setResponseTime(LocalDateTime.now());
        call.setStatus(1);
        return updateById(call);
    }

    @Override
    public boolean complete(Long callId, String result) {
        EmergencyCall call = getById(callId);
        if (call == null || call.getStatus() != 1) {
            return false;
        }
        call.setResult(result);
        call.setStatus(2);
        return updateById(call);
    }

    @Override
    public boolean process(Long callId) {
        EmergencyCall call = getById(callId);
        if (call == null) {
            return false;
        }
        call.setStatus(1);
        call.setResponseTime(LocalDateTime.now());
        return updateById(call);
    }
}
