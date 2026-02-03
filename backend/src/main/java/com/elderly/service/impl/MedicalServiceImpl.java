package com.elderly.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.dto.DoctorListVO;
import com.elderly.dto.MedicalBookingRequest;
import com.elderly.entity.*;
import com.elderly.mapper.*;
import com.elderly.service.HealthRecordService;
import com.elderly.service.MedicalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalServiceImpl implements MedicalService {

    private final MedicalDoctorMapper doctorMapper;
    private final MedicalDoctorDutyMapper dutyMapper;
    private final MedicalAppointmentMapper appointmentMapper;
    private final MedicalCircuitBreakerMapper circuitBreakerMapper;
    private final UserMapper userMapper;
    private final HealthRecordService healthRecordService;
    
    /** 熔断恢复阈值 */
    private static final int CIRCUIT_BREAKER_RECOVERY_THRESHOLD = 5;

    @Override
    public List<DoctorListVO> getAvailableDoctors(LocalDate date) {
        List<DoctorListVO> result = new ArrayList<>();
        
        // 获取当日值班医生
        List<MedicalDoctorDuty> duties = dutyMapper.selectList(
                new LambdaQueryWrapper<MedicalDoctorDuty>()
                        .eq(MedicalDoctorDuty::getDutyDate, date));
        
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        boolean isToday = date.equals(today);
        
        for (MedicalDoctorDuty duty : duties) {
            MedicalDoctor doctor = doctorMapper.selectById(duty.getDoctorId());
            if (doctor == null || doctor.getStatus() != 1) continue;
            
            // 如果医生关联了用户，验证该用户是医疗人员(workerType=3)
            // 如果没有关联用户，则不需要验证（允许独立的医生记录）
            if (doctor.getUserId() != null) {
                User user = userMapper.selectById(doctor.getUserId());
                if (user == null || user.getUserType() != 3 || user.getWorkerType() == null || user.getWorkerType() != 3) {
                    continue; // 跳过关联了非医疗人员的医生
                }
            }
            
            DoctorListVO vo = new DoctorListVO();
            vo.setDoctorId(doctor.getId());
            vo.setName(doctor.getName());
            vo.setTitle(doctor.getTitle());
            vo.setSpecialty(doctor.getSpecialty());
            vo.setAvatar(doctor.getAvatar());
            vo.setDutyType(duty.getDutyType());
            vo.setStartTime(duty.getStartTime().toString());
            vo.setEndTime(duty.getEndTime().toString());
            vo.setIsNightShift(duty.getIsNightShift() != null && duty.getIsNightShift() == 1);
            
            // 根据当前时间判断是否在值班时间段内
            boolean isInDutyTime = checkInDutyTime(duty, now, isToday);
            // 根据值班时间段动态设置状态：
            // 1. 管理员设置为休息(status=0)时，始终显示休息
            // 2. 管理员设置为值班中(status=1)时，根据当前时间判断是否在值班时间段内
            int dutyStatus;
            if (duty.getStatus() == 0) {
                // 管理员设置为休息，始终显示休息
                dutyStatus = 0;
            } else if (isToday) {
                // 今天：根据当前时间判断
                dutyStatus = isInDutyTime ? 1 : 0;
            } else {
                // 未来日期：使用管理员设置的状态
                dutyStatus = duty.getStatus();
            }
            vo.setDutyStatus(dutyStatus);
            
            // 获取熔断状态
            MedicalCircuitBreaker breaker = circuitBreakerMapper.selectByDoctorId(doctor.getId());
            boolean isCircuitOpen = breaker != null && breaker.getIsOpen() == 1;
            
            // 实时统计选中日期的排队人数（只统计状态为0-排队中的预约）
            long queueCount = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<MedicalAppointment>()
                            .eq(MedicalAppointment::getDoctorId, doctor.getId())
                            .eq(MedicalAppointment::getAppointmentDate, date)
                            .eq(MedicalAppointment::getStatus, 0));
            
            vo.setCurrentQueueCount((int) queueCount);
            vo.setMaxQueueLimit(doctor.getMaxQueueLimit());
            
            // 夜间急诊不设熔断限制，但需要在值班时间内且管理员未设置休息
            if (vo.getIsNightShift()) {
                vo.setIsOpen(false);
                // 夜间急诊：管理员设置休息时不可预约；否则如果是今天需要在值班时间内，未来日期可以预约
                vo.setCanBook(duty.getStatus() == 1 && (!isToday || isInDutyTime));
            } else {
                // 日间模式：检查是否在值班时间段内和熔断状态
                vo.setIsOpen(isCircuitOpen);
                // 管理员设置休息时不可预约；否则如果是今天需要在值班时间内，未来日期可以预约
                vo.setCanBook(duty.getStatus() == 1 && (!isToday || isInDutyTime) && !isCircuitOpen && queueCount < doctor.getMaxQueueLimit());
            }
            
            result.add(vo);
        }
        
        return result;
    }
    
    /**
     * 检查当前时间是否在值班时间段内
     */
    private boolean checkInDutyTime(MedicalDoctorDuty duty, LocalTime now, boolean isToday) {
        if (!isToday) {
            return true; // 非今天的值班，不需要检查时间
        }
        
        LocalTime startTime = duty.getStartTime();
        LocalTime endTime = duty.getEndTime();
        
        // 夜间值班（跨天情况，如20:00-08:00）
        if (duty.getIsNightShift() != null && duty.getIsNightShift() == 1) {
            // 夜间值班：20:00-24:00 或 00:00-08:00 都算在值班时间内
            if (startTime.isAfter(endTime)) {
                // 跨天：当前时间 >= 开始时间 或 当前时间 < 结束时间
                return !now.isBefore(startTime) || now.isBefore(endTime);
            }
        }
        
        // 日间值班或不跨天的情况
        return !now.isBefore(startTime) && now.isBefore(endTime);
    }
    
    /**
     * 判断当前时间是否在日间时段(8:00-20:00)
     */
    private boolean isInDayTime(LocalTime time) {
        return !time.isBefore(LocalTime.of(8, 0)) && time.isBefore(LocalTime.of(20, 0));
    }

    @Override
    public boolean checkDoctorAvailable(Long doctorId, LocalDate date) {
        MedicalDoctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null || doctor.getStatus() != 1) return false;
        
        // 检查是否值班
        MedicalDoctorDuty duty = dutyMapper.selectOne(
                new LambdaQueryWrapper<MedicalDoctorDuty>()
                        .eq(MedicalDoctorDuty::getDoctorId, doctorId)
                        .eq(MedicalDoctorDuty::getDutyDate, date));
        if (duty == null) return false;
        
        // 管理员设置为休息时不可预约
        if (duty.getStatus() == 0) return false;
        
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        boolean isToday = date.equals(today);
        
        // 检查是否在值班时间段内
        boolean isInDutyTime = checkInDutyTime(duty, now, isToday);
        
        // 夜间急诊模式
        if (duty.getIsNightShift() != null && duty.getIsNightShift() == 1) {
            // 夜间急诊：如果是今天需要在值班时间内，未来日期可以预约
            return !isToday || isInDutyTime;
        }
        
        // 日间模式：如果是今天需要在值班时间内
        if (isToday && !isInDutyTime) {
            return false;
        }
        
        // 检查熔断状态（仅针对当天的预约检查熔断）
        if (isToday) {
            MedicalCircuitBreaker breaker = circuitBreakerMapper.selectByDoctorId(doctorId);
            if (breaker != null && breaker.getIsOpen() == 1) return false;
        }
        
        // 实时统计指定日期的排队人数（只统计状态为0-排队中的预约）
        long queueCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getDoctorId, doctorId)
                        .eq(MedicalAppointment::getAppointmentDate, date)
                        .eq(MedicalAppointment::getStatus, 0));
        return queueCount < doctor.getMaxQueueLimit();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAppointment(MedicalBookingRequest request, Long userId) {
        // 检查医生是否可预约
        if (!checkDoctorAvailable(request.getDoctorId(), request.getAppointmentDate())) {
            throw new RuntimeException("该医生当前不可预约");
        }
        
        Long elderlyId = request.getElderlyId() != null ? request.getElderlyId() : userId;
        
        // 检查是否重复预约
        long existCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getElderlyId, elderlyId)
                        .eq(MedicalAppointment::getDoctorId, request.getDoctorId())
                        .eq(MedicalAppointment::getAppointmentDate, request.getAppointmentDate())
                        .in(MedicalAppointment::getStatus, 0, 1));
        if (existCount > 0) {
            throw new RuntimeException("您已预约该医生，请勿重复预约");
        }
        
        // 获取排队号
        int queueNumber = getNextQueueNumber(request.getDoctorId(), request.getAppointmentDate());
        
        // 创建预约
        MedicalAppointment appointment = new MedicalAppointment();
        appointment.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        appointment.setElderlyId(elderlyId);
        appointment.setBookerId(userId);
        appointment.setDoctorId(request.getDoctorId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentType(request.getAppointmentType() != null ? request.getAppointmentType() : 1);
        appointment.setQueueNumber(queueNumber);
        appointment.setSymptoms(request.getSymptoms());
        appointment.setAddress(request.getAddress());
        appointment.setRemark(request.getRemark());
        appointment.setStatus(0);
        
        // 生成6位服务码
        appointment.setServiceCode(String.valueOf((int) ((Math.random() * 900000) + 100000)));
        
        appointmentMapper.insert(appointment);
        
        // 归档症状至简易健康档案
        if (request.getSymptoms() != null && !request.getSymptoms().isEmpty()) {
            healthRecordService.saveHealthInfo(
                    elderlyId,
                    appointment.getId(),
                    request.getSymptoms(),
                    null,  // 过敏史（可后续扩展）
                    null   // 常服药品（可后续扩展）
            );
        }
        
        // 更新熔断状态
        updateCircuitBreakerStatus(request.getDoctorId());
        
        return appointment.getId();
    }

    private int getNextQueueNumber(Long doctorId, LocalDate date) {
        Integer maxNumber = appointmentMapper.selectMaxQueueNumber(doctorId, date);
        return maxNumber != null ? maxNumber + 1 : 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelAppointment(Long appointmentId, Long userId) {
        MedicalAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) return false;
        if (!appointment.getElderlyId().equals(userId) && !appointment.getBookerId().equals(userId)) {
            throw new RuntimeException("无权操作此预约");
        }
        if (appointment.getStatus() >= 2) {
            throw new RuntimeException("预约状态不允许取消");
        }
        
        appointment.setStatus(5); // 已取消
        appointment.setCancelTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
        
        // 更新熔断状态（传入预约日期，确保正确更新对应日期的排队人数）
        updateCircuitBreakerStatusForDate(appointment.getDoctorId(), appointment.getAppointmentDate());
        
        return true;
    }

    @Override
    public List<MedicalAppointment> getUserAppointments(Long userId, Integer status) {
        LambdaQueryWrapper<MedicalAppointment> wrapper = new LambdaQueryWrapper<MedicalAppointment>()
                .eq(MedicalAppointment::getElderlyId, userId)
                .eq(status != null, MedicalAppointment::getStatus, status)
                .orderByDesc(MedicalAppointment::getCreateTime);
        
        List<MedicalAppointment> appointments = appointmentMapper.selectList(wrapper);
        for (MedicalAppointment apt : appointments) {
            apt.setDoctor(doctorMapper.selectById(apt.getDoctorId()));
        }
        return appointments;
    }

    @Override
    public void updateCircuitBreakerStatus(Long doctorId) {
        // 默认更新当天的熔断状态
        updateCircuitBreakerStatusForDate(doctorId, LocalDate.now());
    }
    
    /**
     * 更新指定日期的熔断状态
     * 注意：熔断器表只存储当天的状态，但排队人数需要根据指定日期实时统计
     */
    private void updateCircuitBreakerStatusForDate(Long doctorId, LocalDate date) {
        MedicalDoctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) return;
        
        LocalDate today = LocalDate.now();
        boolean isToday = date.equals(today);
        
        // 检查是否是夜间值班医生
        MedicalDoctorDuty duty = dutyMapper.selectOne(
                new LambdaQueryWrapper<MedicalDoctorDuty>()
                        .eq(MedicalDoctorDuty::getDoctorId, doctorId)
                        .eq(MedicalDoctorDuty::getDutyDate, date));
        
        boolean isNightShift = duty != null && duty.getIsNightShift() != null && duty.getIsNightShift() == 1;
        
        // 统计指定日期的排队人数（只统计状态为0-排队中的预约）
        long queueCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getDoctorId, doctorId)
                        .eq(MedicalAppointment::getAppointmentDate, date)
                        .eq(MedicalAppointment::getStatus, 0));
        
        // 熔断状态只对当天有效，非当天的预约不影响熔断器
        if (!isToday) {
            // 非当天的预约变更，不更新熔断器状态
            return;
        }
        
        // 判断是否在值班时间段内
        boolean isInDutyTime = duty != null && checkInDutyTime(duty, LocalTime.now(), true);
        
        // 计算熔断状态
        // 熔断规则：日间(8:00-20:00)排队人数 > Max_Limit 时触发熔断；排队人数 < 5人时自动恢复
        // 夜间急诊模式不设熔断限制
        int isOpen;
        LocalTime now = LocalTime.now();
        boolean isDayTime = isInDayTime(now);
        
        if (isNightShift) {
            // 夜间急诊不设熔断
            isOpen = 0;
        } else if (!isDayTime) {
            // 夜间时段(20:00-8:00)不设熔断限制
            isOpen = 0;
        } else if (queueCount > doctor.getMaxQueueLimit()) {
            // 日间模式：排队人数 > 最大限制，触发熔断（注意是 > 不是 >=）
            isOpen = 1;
        } else if (queueCount < CIRCUIT_BREAKER_RECOVERY_THRESHOLD) {
            // 排队人数 < 5人，自动恢复接单
            isOpen = 0;
        } else {
            // 保持当前状态（5 <= queueCount <= maxLimit 时不自动变化）
            MedicalCircuitBreaker existing = circuitBreakerMapper.selectByDoctorId(doctorId);
            isOpen = existing != null ? existing.getIsOpen() : 0;
        }
        
        MedicalCircuitBreaker breaker = circuitBreakerMapper.selectByDoctorId(doctorId);
        if (breaker == null) {
            breaker = new MedicalCircuitBreaker();
            breaker.setDoctorId(doctorId);
            breaker.setCurrentQueueCount((int) queueCount);
            breaker.setIsOpen(isOpen);
            breaker.setLastUpdateTime(LocalDateTime.now());
            circuitBreakerMapper.insert(breaker);
        } else {
            breaker.setCurrentQueueCount((int) queueCount);
            breaker.setIsOpen(isOpen);
            breaker.setLastUpdateTime(LocalDateTime.now());
            circuitBreakerMapper.updateById(breaker);
        }
    }

    @Override
    public boolean startVisit(Long appointmentId, Long doctorId) {
        MedicalAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || !appointment.getDoctorId().equals(doctorId)) {
            return false;
        }
        if (appointment.getStatus() != 0) {
            return false;
        }
        
        appointment.setStatus(1);
        appointment.setVisitTime(LocalDateTime.now());
        return appointmentMapper.updateById(appointment) > 0;
    }

    @Override
    public boolean completeVisit(Long appointmentId, String diagnosis, String prescription) {
        MedicalAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || appointment.getStatus() != 1) {
            return false;
        }
        
        appointment.setStatus(2);
        appointment.setDiagnosis(diagnosis);
        appointment.setPrescription(prescription);
        appointmentMapper.updateById(appointment);
        
        // 更新熔断状态（传入预约日期）
        updateCircuitBreakerStatusForDate(appointment.getDoctorId(), appointment.getAppointmentDate());
        
        return true;
    }
    
    @Override
    public boolean confirmAppointment(Long appointmentId, Long userId) {
        MedicalAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || appointment.getDeleted() == 1) {
            throw new RuntimeException("预约不存在");
        }
        if (!appointment.getElderlyId().equals(userId) && !appointment.getBookerId().equals(userId)) {
            throw new RuntimeException("无权操作此预约");
        }
        // 状态2是已完成/待确认
        if (appointment.getStatus() != 2) {
            throw new RuntimeException("预约状态不正确，只有已完成状态才能确认");
        }
        appointment.setStatus(3); // 已确认完成
        return appointmentMapper.updateById(appointment) > 0;
    }
    
    @Override
    public boolean disputeAppointment(Long appointmentId, Long userId, String reason) {
        MedicalAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || appointment.getDeleted() == 1) {
            throw new RuntimeException("预约不存在");
        }
        if (!appointment.getElderlyId().equals(userId) && !appointment.getBookerId().equals(userId)) {
            throw new RuntimeException("无权操作此预约");
        }
        // 状态2是已完成/待确认
        if (appointment.getStatus() != 2) {
            throw new RuntimeException("预约状态不正确，只有已完成状态才能提交争议");
        }
        appointment.setStatus(4); // 争议中
        appointment.setRemark((appointment.getRemark() != null ? appointment.getRemark() + " | " : "") + "争议原因: " + reason);
        return appointmentMapper.updateById(appointment) > 0;
    }
    
    @Override
    public boolean resolveDispute(Long appointmentId) {
        MedicalAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || appointment.getDeleted() == 1) {
            throw new RuntimeException("预约不存在");
        }
        if (appointment.getStatus() != 4) {
            throw new RuntimeException("预约状态不正确，只有争议中状态才能处理");
        }
        appointment.setStatus(3); // 已确认完成
        appointment.setRemark((appointment.getRemark() != null ? appointment.getRemark() + " | " : "") + "争议已处理");
        return appointmentMapper.updateById(appointment) > 0;
    }
}
