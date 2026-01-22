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
    
    /** 日间时段开始时间 */
    private static final LocalTime DAY_START = LocalTime.of(8, 0);
    /** 日间时段结束时间 */
    private static final LocalTime DAY_END = LocalTime.of(20, 0);
    /** 熔断恢复阈值 */
    private static final int CIRCUIT_BREAKER_RECOVERY_THRESHOLD = 5;

    @Override
    public List<DoctorListVO> getAvailableDoctors(LocalDate date) {
        List<DoctorListVO> result = new ArrayList<>();
        
        // 获取当日值班医生
        List<MedicalDoctorDuty> duties = dutyMapper.selectList(
                new LambdaQueryWrapper<MedicalDoctorDuty>()
                        .eq(MedicalDoctorDuty::getDutyDate, date)
                        .eq(MedicalDoctorDuty::getStatus, 1));
        
        LocalTime now = LocalTime.now();
        boolean isDayTime = isInDayTime(now);
        
        for (MedicalDoctorDuty duty : duties) {
            MedicalDoctor doctor = doctorMapper.selectById(duty.getDoctorId());
            if (doctor == null || doctor.getStatus() != 1) continue;
            
            // 验证关联的用户是医疗人员(workerType=3)
            User user = userMapper.selectById(doctor.getUserId());
            if (user == null || user.getUserType() != 3 || (user.getWorkerType() != null && user.getWorkerType() != 3)) {
                continue; // 跳过非医疗人员
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
            
            // 获取熔断状态
            MedicalCircuitBreaker breaker = circuitBreakerMapper.selectByDoctorId(doctor.getId());
            int queueCount = breaker != null ? breaker.getCurrentQueueCount() : 0;
            boolean isCircuitOpen = breaker != null && breaker.getIsOpen() == 1;
            
            vo.setCurrentQueueCount(queueCount);
            vo.setMaxQueueLimit(doctor.getMaxQueueLimit());
            
            // 夜间急诊不设熔断限制
            if (vo.getIsNightShift()) {
                vo.setIsOpen(false);
                vo.setCanBook(true);
            } else {
                // 日间模式：检查时间段和熔断状态
                vo.setIsOpen(isCircuitOpen);
                vo.setCanBook(isDayTime && !isCircuitOpen && queueCount < doctor.getMaxQueueLimit());
            }
            
            result.add(vo);
        }
        
        return result;
    }
    
    /**
     * 判断当前时间是否在日间时段(8:00-20:00)
     */
    private boolean isInDayTime(LocalTime time) {
        return !time.isBefore(DAY_START) && time.isBefore(DAY_END);
    }

    @Override
    public boolean checkDoctorAvailable(Long doctorId, LocalDate date) {
        MedicalDoctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null || doctor.getStatus() != 1) return false;
        
        // 检查是否值班
        MedicalDoctorDuty duty = dutyMapper.selectOne(
                new LambdaQueryWrapper<MedicalDoctorDuty>()
                        .eq(MedicalDoctorDuty::getDoctorId, doctorId)
                        .eq(MedicalDoctorDuty::getDutyDate, date)
                        .eq(MedicalDoctorDuty::getStatus, 1));
        if (duty == null) return false;
        
        // 夜间急诊模式不设熔断限制
        if (duty.getIsNightShift() != null && duty.getIsNightShift() == 1) {
            return true;
        }
        
        // 日间模式：检查是否在8:00-20:00时间段内
        LocalTime now = LocalTime.now();
        if (!isInDayTime(now)) {
            return false; // 非日间时段，日间医生停止接单
        }
        
        // 检查熔断状态
        MedicalCircuitBreaker breaker = circuitBreakerMapper.selectByDoctorId(doctorId);
        if (breaker != null && breaker.getIsOpen() == 1) return false;
        
        // 检查排队人数
        int queueCount = breaker != null ? breaker.getCurrentQueueCount() : 0;
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
        
        appointment.setStatus(3);
        appointment.setCancelTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
        
        // 更新熔断状态
        updateCircuitBreakerStatus(appointment.getDoctorId());
        
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
        MedicalDoctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) return;
        
        // 检查是否是夜间值班医生，夜间值班不设熔断
        MedicalDoctorDuty duty = dutyMapper.selectOne(
                new LambdaQueryWrapper<MedicalDoctorDuty>()
                        .eq(MedicalDoctorDuty::getDoctorId, doctorId)
                        .eq(MedicalDoctorDuty::getDutyDate, LocalDate.now())
                        .eq(MedicalDoctorDuty::getStatus, 1));
        
        boolean isNightShift = duty != null && duty.getIsNightShift() != null && duty.getIsNightShift() == 1;
        
        // 统计当前排队人数
        long queueCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getDoctorId, doctorId)
                        .eq(MedicalAppointment::getAppointmentDate, LocalDate.now())
                        .eq(MedicalAppointment::getStatus, 0));
        
        // 判断是否在日间时段
        boolean isDayTime = isInDayTime(LocalTime.now());
        
        // 计算熔断状态
        int isOpen;
        if (isNightShift) {
            // 夜间急诊不设熔断
            isOpen = 0;
        } else if (!isDayTime) {
            // 非日间时段，触发熔断停止接单
            isOpen = 1;
        } else if (queueCount >= doctor.getMaxQueueLimit()) {
            // 排队人数 >= 最大限制，触发熔断
            isOpen = 1;
        } else if (queueCount < CIRCUIT_BREAKER_RECOVERY_THRESHOLD) {
            // 排队人数 < 5人，自动恢复接单
            isOpen = 0;
        } else {
            // 保持当前状态（5 <= queueCount < maxLimit 时不自动变化）
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
        
        // 更新熔断状态
        updateCircuitBreakerStatus(appointment.getDoctorId());
        
        return true;
    }
}
