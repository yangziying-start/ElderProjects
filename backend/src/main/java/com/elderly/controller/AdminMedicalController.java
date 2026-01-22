package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.entity.*;
import com.elderly.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 医疗巡诊管理
 */
@RestController
@RequestMapping("/api/admin/medical")
@RequiredArgsConstructor
public class AdminMedicalController {

    private final MedicalDoctorMapper doctorMapper;
    private final MedicalDoctorDutyMapper doctorDutyMapper;
    private final MedicalCircuitBreakerMapper circuitBreakerMapper;
    private final MedicalAppointmentMapper appointmentMapper;
    private final UserMapper userMapper;

    // ========== 医疗人员列表（用于关联医生） ==========

    @GetMapping("/medical-workers")
    public Result<List<User>> listMedicalWorkers() {
        // 获取用户类型为服务人员且服务人员类型为医疗人员的用户 (userType=3, workerType=3)
        return Result.success(userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUserType, 3)
                        .eq(User::getWorkerType, 3)  // 只返回医疗人员
                        .eq(User::getStatus, 1)
                        .orderByDesc(User::getCreateTime)));
    }

    // ========== 医生信息管理 ==========

    @GetMapping("/doctors")
    public Result<List<MedicalDoctor>> listDoctors() {
        List<MedicalDoctor> doctors = doctorMapper.selectList(
                new LambdaQueryWrapper<MedicalDoctor>()
                        .orderByDesc(MedicalDoctor::getCreateTime));
        // 填充熔断状态和排队人数
        doctors.forEach(this::fillDoctorStatus);
        return Result.success(doctors);
    }

    @GetMapping("/doctors/page")
    public Result<Page<MedicalDoctor>> pageDoctors(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<MedicalDoctor> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MedicalDoctor> wrapper = new LambdaQueryWrapper<MedicalDoctor>()
                .like(name != null, MedicalDoctor::getName, name)
                .eq(status != null, MedicalDoctor::getStatus, status)
                .orderByDesc(MedicalDoctor::getCreateTime);
        Page<MedicalDoctor> result = doctorMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::fillDoctorStatus);
        return Result.success(result);
    }

    @GetMapping("/doctors/{id}")
    public Result<MedicalDoctor> getDoctor(@PathVariable Long id) {
        MedicalDoctor doctor = doctorMapper.selectById(id);
        if (doctor != null) {
            fillDoctorStatus(doctor);
        }
        return Result.success(doctor);
    }

    @PostMapping("/doctors")
    public Result<MedicalDoctor> addDoctor(@RequestBody MedicalDoctor doctor) {
        // 验证关联用户必须是医疗人员 (workerType=3)
        if (doctor.getUserId() != null) {
            User user = userMapper.selectById(doctor.getUserId());
            if (user == null) {
                return Result.error("关联用户不存在");
            }
            if (user.getUserType() != 3 || user.getWorkerType() == null || user.getWorkerType() != 3) {
                return Result.error("关联用户必须是医疗人员类型");
            }
            // 检查该用户是否已关联其他医生
            Long existingDoctor = doctorMapper.selectCount(
                    new LambdaQueryWrapper<MedicalDoctor>().eq(MedicalDoctor::getUserId, doctor.getUserId()));
            if (existingDoctor > 0) {
                return Result.error("该医疗人员已关联其他医生");
            }
        }
        if (doctor.getStatus() == null) {
            doctor.setStatus(1);
        }
        if (doctor.getMaxQueueLimit() == null) {
            doctor.setMaxQueueLimit(10);
        }
        doctorMapper.insert(doctor);
        // 初始化熔断状态
        MedicalCircuitBreaker breaker = new MedicalCircuitBreaker();
        breaker.setDoctorId(doctor.getId());
        breaker.setCurrentQueueCount(0);
        breaker.setIsOpen(0);
        breaker.setLastUpdateTime(LocalDateTime.now());
        circuitBreakerMapper.insert(breaker);
        return Result.success(doctor);
    }

    @PutMapping("/doctors/{id}")
    public Result<Void> updateDoctor(@PathVariable Long id, @RequestBody MedicalDoctor doctor) {
        // 验证关联用户必须是医疗人员 (workerType=3)
        if (doctor.getUserId() != null) {
            User user = userMapper.selectById(doctor.getUserId());
            if (user == null) {
                return Result.error("关联用户不存在");
            }
            if (user.getUserType() != 3 || user.getWorkerType() == null || user.getWorkerType() != 3) {
                return Result.error("关联用户必须是医疗人员类型");
            }
            // 检查该用户是否已关联其他医生（排除当前医生）
            Long existingDoctor = doctorMapper.selectCount(
                    new LambdaQueryWrapper<MedicalDoctor>()
                            .eq(MedicalDoctor::getUserId, doctor.getUserId())
                            .ne(MedicalDoctor::getId, id));
            if (existingDoctor > 0) {
                return Result.error("该医疗人员已关联其他医生");
            }
        }
        doctor.setId(id);
        doctorMapper.updateById(doctor);
        return Result.success();
    }

    @PostMapping("/doctors/{id}/status")
    public Result<Void> updateDoctorStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        MedicalDoctor doctor = new MedicalDoctor();
        doctor.setId(id);
        doctor.setStatus(body.get("status"));
        doctorMapper.updateById(doctor);
        return Result.success();
    }

    @DeleteMapping("/doctors/{id}")
    public Result<Void> deleteDoctor(@PathVariable Long id) {
        // 检查是否有关联预约
        long appointmentCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<MedicalAppointment>().eq(MedicalAppointment::getDoctorId, id));
        if (appointmentCount > 0) {
            return Result.error("该医生有关联预约，无法删除");
        }
        doctorMapper.deleteById(id);
        // 删除熔断状态
        circuitBreakerMapper.delete(
                new LambdaQueryWrapper<MedicalCircuitBreaker>().eq(MedicalCircuitBreaker::getDoctorId, id));
        return Result.success();
    }

    // ========== 医生值班管理 ==========

    @GetMapping("/duties")
    public Result<List<MedicalDoctorDuty>> listDuties(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<MedicalDoctorDuty> wrapper = new LambdaQueryWrapper<MedicalDoctorDuty>()
                .eq(doctorId != null, MedicalDoctorDuty::getDoctorId, doctorId)
                .ge(startDate != null, MedicalDoctorDuty::getDutyDate, startDate)
                .le(endDate != null, MedicalDoctorDuty::getDutyDate, endDate)
                .orderByAsc(MedicalDoctorDuty::getDutyDate)
                .orderByAsc(MedicalDoctorDuty::getStartTime);
        return Result.success(doctorDutyMapper.selectList(wrapper));
    }

    @GetMapping("/duties/{id}")
    public Result<MedicalDoctorDuty> getDuty(@PathVariable Long id) {
        return Result.success(doctorDutyMapper.selectById(id));
    }

    @PostMapping("/duties")
    public Result<MedicalDoctorDuty> addDuty(@RequestBody MedicalDoctorDuty duty) {
        // 根据值班类型设置默认时间
        if (duty.getDutyType() == 1) { // 日间
            if (duty.getStartTime() == null) duty.setStartTime(LocalTime.of(8, 0));
            if (duty.getEndTime() == null) duty.setEndTime(LocalTime.of(20, 0));
            duty.setIsNightShift(0);
        } else if (duty.getDutyType() == 2) { // 夜间
            if (duty.getStartTime() == null) duty.setStartTime(LocalTime.of(20, 0));
            if (duty.getEndTime() == null) duty.setEndTime(LocalTime.of(8, 0));
            duty.setIsNightShift(1);
        }
        if (duty.getStatus() == null) {
            duty.setStatus(1);
        }
        doctorDutyMapper.insert(duty);
        return Result.success(duty);
    }

    @PostMapping("/duties/batch")
    public Result<Void> batchAddDuties(@RequestBody List<MedicalDoctorDuty> duties) {
        for (MedicalDoctorDuty duty : duties) {
            if (duty.getDutyType() == 1) {
                if (duty.getStartTime() == null) duty.setStartTime(LocalTime.of(8, 0));
                if (duty.getEndTime() == null) duty.setEndTime(LocalTime.of(20, 0));
                duty.setIsNightShift(0);
            } else if (duty.getDutyType() == 2) {
                if (duty.getStartTime() == null) duty.setStartTime(LocalTime.of(20, 0));
                if (duty.getEndTime() == null) duty.setEndTime(LocalTime.of(8, 0));
                duty.setIsNightShift(1);
            }
            if (duty.getStatus() == null) {
                duty.setStatus(1);
            }
            doctorDutyMapper.insert(duty);
        }
        return Result.success();
    }

    @PutMapping("/duties/{id}")
    public Result<Void> updateDuty(@PathVariable Long id, @RequestBody MedicalDoctorDuty duty) {
        duty.setId(id);
        doctorDutyMapper.updateById(duty);
        return Result.success();
    }

    @DeleteMapping("/duties/{id}")
    public Result<Void> deleteDuty(@PathVariable Long id) {
        MedicalDoctorDuty duty = doctorDutyMapper.selectById(id);
        if (duty != null) {
            // 检查是否有关联预约
            long appointmentCount = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<MedicalAppointment>()
                            .eq(MedicalAppointment::getDoctorId, duty.getDoctorId())
                            .eq(MedicalAppointment::getAppointmentDate, duty.getDutyDate())
                            .ne(MedicalAppointment::getStatus, 3)); // 排除已取消的
            if (appointmentCount > 0) {
                return Result.error("该值班有关联预约，无法删除");
            }
        }
        doctorDutyMapper.deleteById(id);
        return Result.success();
    }

    // ========== 熔断状态监控 ==========

    @GetMapping("/circuit-breakers")
    public Result<List<Map<String, Object>>> listCircuitBreakers() {
        List<MedicalDoctor> doctors = doctorMapper.selectList(
                new LambdaQueryWrapper<MedicalDoctor>().eq(MedicalDoctor::getStatus, 1));
        List<Map<String, Object>> result = new ArrayList<>();
        for (MedicalDoctor doctor : doctors) {
            fillDoctorStatus(doctor);
            result.add(Map.of(
                    "doctorId", doctor.getId(),
                    "doctorName", doctor.getName(),
                    "title", doctor.getTitle() != null ? doctor.getTitle() : "",
                    "maxQueueLimit", doctor.getMaxQueueLimit(),
                    "currentQueueCount", doctor.getCurrentQueueCount() != null ? doctor.getCurrentQueueCount() : 0,
                    "isCircuitOpen", doctor.getIsCircuitOpen() != null ? doctor.getIsCircuitOpen() : false
            ));
        }
        return Result.success(result);
    }

    @PostMapping("/circuit-breakers/{doctorId}/reset")
    public Result<Void> resetCircuitBreaker(@PathVariable Long doctorId) {
        MedicalCircuitBreaker breaker = circuitBreakerMapper.selectOne(
                new LambdaQueryWrapper<MedicalCircuitBreaker>()
                        .eq(MedicalCircuitBreaker::getDoctorId, doctorId));
        if (breaker != null) {
            breaker.setIsOpen(0);
            breaker.setLastUpdateTime(LocalDateTime.now());
            circuitBreakerMapper.updateById(breaker);
        }
        return Result.success();
    }

    // ========== 医疗预约管理 ==========

    @GetMapping("/appointments")
    public Result<Page<MedicalAppointment>> pageAppointments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String appointmentDate,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Integer status) {
        Page<MedicalAppointment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MedicalAppointment> wrapper = new LambdaQueryWrapper<MedicalAppointment>()
                .eq(appointmentDate != null, MedicalAppointment::getAppointmentDate, appointmentDate)
                .eq(doctorId != null, MedicalAppointment::getDoctorId, doctorId)
                .eq(status != null, MedicalAppointment::getStatus, status)
                .orderByDesc(MedicalAppointment::getCreateTime);
        Page<MedicalAppointment> result = appointmentMapper.selectPage(pageParam, wrapper);
        // 填充医生信息
        result.getRecords().forEach(appointment -> {
            if (appointment.getDoctorId() != null) {
                appointment.setDoctor(doctorMapper.selectById(appointment.getDoctorId()));
            }
        });
        return Result.success(result);
    }

    @GetMapping("/appointments/{id}")
    public Result<MedicalAppointment> getAppointment(@PathVariable Long id) {
        MedicalAppointment appointment = appointmentMapper.selectById(id);
        if (appointment != null && appointment.getDoctorId() != null) {
            appointment.setDoctor(doctorMapper.selectById(appointment.getDoctorId()));
        }
        return Result.success(appointment);
    }

    @PostMapping("/appointments/{id}/status")
    public Result<Void> updateAppointmentStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        MedicalAppointment appointment = new MedicalAppointment();
        appointment.setId(id);
        appointment.setStatus((Integer) body.get("status"));
        if (body.containsKey("diagnosis")) {
            appointment.setDiagnosis((String) body.get("diagnosis"));
        }
        if (body.containsKey("prescription")) {
            appointment.setPrescription((String) body.get("prescription"));
        }
        appointmentMapper.updateById(appointment);
        return Result.success();
    }

    @GetMapping("/appointments/stats")
    public Result<Map<String, Object>> getAppointmentStats() {
        LocalDate today = LocalDate.now();
        // 今日预约数
        long todayCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<MedicalAppointment>().eq(MedicalAppointment::getAppointmentDate, today));
        // 排队中数
        long queueingCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getAppointmentDate, today)
                        .eq(MedicalAppointment::getStatus, 0));
        // 巡诊中数
        long visitingCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getAppointmentDate, today)
                        .eq(MedicalAppointment::getStatus, 1));
        return Result.success(Map.of(
                "todayCount", todayCount,
                "queueingCount", queueingCount,
                "visitingCount", visitingCount));
    }

    // ========== 健康档案 ==========

    @GetMapping("/health-records/{elderlyId}")
    public Result<List<MedicalAppointment>> getHealthRecords(@PathVariable Long elderlyId) {
        List<MedicalAppointment> records = appointmentMapper.selectList(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getElderlyId, elderlyId)
                        .eq(MedicalAppointment::getStatus, 2) // 已完成的
                        .orderByDesc(MedicalAppointment::getAppointmentDate));
        records.forEach(r -> {
            if (r.getDoctorId() != null) {
                r.setDoctor(doctorMapper.selectById(r.getDoctorId()));
            }
        });
        return Result.success(records);
    }

    // ========== 辅助方法 ==========

    private void fillDoctorStatus(MedicalDoctor doctor) {
        // 获取熔断状态
        MedicalCircuitBreaker breaker = circuitBreakerMapper.selectOne(
                new LambdaQueryWrapper<MedicalCircuitBreaker>()
                        .eq(MedicalCircuitBreaker::getDoctorId, doctor.getId()));
        if (breaker != null) {
            doctor.setCurrentQueueCount(breaker.getCurrentQueueCount());
            doctor.setIsCircuitOpen(breaker.getIsOpen() == 1);
        } else {
            doctor.setCurrentQueueCount(0);
            doctor.setIsCircuitOpen(false);
        }
        // 获取今日值班信息
        MedicalDoctorDuty todayDuty = doctorDutyMapper.selectOne(
                new LambdaQueryWrapper<MedicalDoctorDuty>()
                        .eq(MedicalDoctorDuty::getDoctorId, doctor.getId())
                        .eq(MedicalDoctorDuty::getDutyDate, LocalDate.now())
                        .eq(MedicalDoctorDuty::getStatus, 1));
        doctor.setTodayDuty(todayDuty);
    }
}
