package com.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderly.entity.EmergencyCall;

public interface EmergencyService extends IService<EmergencyCall> {
    Long triggerCall(Long elderlyId, Integer eventType);
    boolean respond(Long callId, Long responderId);
    boolean complete(Long callId, String result);
    boolean process(Long callId);
}
