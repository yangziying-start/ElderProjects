package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.common.Result;
import com.elderly.entity.HealthRecord;
import com.elderly.entity.MedicalAppointment;
import com.elderly.entity.MedicalDoctor;
import com.elderly.entity.User;
import com.elderly.mapper.HealthRecordMapper;
import com.elderly.mapper.MedicalAppointmentMapper;
import com.elderly.mapper.MedicalDoctorMapper;
import com.elderly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class HealthController {

    private final UserService userService;
    private final HealthRecordMapper healthRecordMapper;
    private final MedicalAppointmentMapper medicalAppointmentMapper;
    private final MedicalDoctorMapper medicalDoctorMapper;

    /**
     * 获取老人健康档案基本信息
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> getHealthInfo(@RequestParam Long elderlyId) {
        User elderly = userService.getById(elderlyId);
        if (elderly == null) {
            throw new RuntimeException("老人不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("name", elderly.getName());
        result.put("phone", elderly.getPhone());
        result.put("address", elderly.getAddress());
        
        // 从身份证解析性别和生日
        String idCard = elderly.getIdCard();
        if (idCard != null && idCard.length() >= 17) {
            // 性别：身份证第17位，奇数为男，偶数为女
            int genderCode = Integer.parseInt(idCard.substring(16, 17));
            result.put("gender", genderCode % 2 == 1 ? "男" : "女");
            
            // 生日：身份证第7-14位
            String birthStr = idCard.substring(6, 14);
            String birthday = birthStr.substring(0, 4) + "-" + birthStr.substring(4, 6) + "-" + birthStr.substring(6, 8);
            result.put("birthday", birthday);
            
            // 计算年龄
            LocalDate birthDate = LocalDate.parse(birthday);
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            result.put("age", age);
        }
        
        // 统计预约次数和完成巡诊次数
        long appointmentCount = medicalAppointmentMapper.selectCount(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getElderlyId, elderlyId));
        long completedCount = medicalAppointmentMapper.selectCount(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getElderlyId, elderlyId)
                        .eq(MedicalAppointment::getStatus, 2));
        result.put("appointmentCount", appointmentCount);
        result.put("completedCount", completedCount);
        
        // 获取最新的健康记录（过敏史、常服药品）
        HealthRecord latestRecord = healthRecordMapper.selectOne(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getElderlyId, elderlyId)
                        .orderByDesc(HealthRecord::getCreateTime)
                        .last("LIMIT 1"));
        if (latestRecord != null) {
            result.put("allergies", latestRecord.getAllergies());
            result.put("medications", latestRecord.getMedications());
        }
        
        return Result.success(result);
    }

    /**
     * 获取老人医疗记录列表
     */
    @GetMapping("/health/records")
    public Result<List<Map<String, Object>>> getHealthRecords(@RequestParam Long elderlyId) {
        // 查询医疗预约记录
        List<MedicalAppointment> appointments = medicalAppointmentMapper.selectList(
                new LambdaQueryWrapper<MedicalAppointment>()
                        .eq(MedicalAppointment::getElderlyId, elderlyId)
                        .orderByDesc(MedicalAppointment::getCreateTime));
        
        return Result.success(appointments.stream().map(apt -> {
            Map<String, Object> record = new HashMap<>();
            record.put("id", apt.getId());
            record.put("appointmentDate", apt.getAppointmentDate() != null ? apt.getAppointmentDate().toString() : null);
            record.put("appointmentType", apt.getAppointmentType());
            record.put("appointmentTypeName", apt.getAppointmentType() == 1 ? "日间巡诊" : "夜间急诊");
            record.put("symptoms", apt.getSymptoms());
            record.put("diagnosis", apt.getDiagnosis());
            record.put("prescription", apt.getPrescription());
            record.put("status", apt.getStatus());
            record.put("statusName", getStatusName(apt.getStatus()));
            
            // 获取医生信息
            if (apt.getDoctorId() != null) {
                MedicalDoctor doctor = medicalDoctorMapper.selectById(apt.getDoctorId());
                if (doctor != null) {
                    record.put("doctorName", doctor.getName());
                }
            }
            
            return record;
        }).collect(Collectors.toList()));
    }
    
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "排队中";
            case 1: return "巡诊中";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "未知";
        }
    }

    /**
     * 获取医疗记录详情
     */
    @GetMapping("/health/records/{id}")
    public Result<Map<String, Object>> getHealthRecordDetail(@PathVariable Long id) {
        MedicalAppointment apt = medicalAppointmentMapper.selectById(id);
        if (apt == null) {
            throw new RuntimeException("记录不存在");
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", apt.getId());
        detail.put("orderNo", apt.getOrderNo());
        detail.put("appointmentDate", apt.getAppointmentDate() != null ? apt.getAppointmentDate().toString() : null);
        detail.put("appointmentType", apt.getAppointmentType());
        detail.put("appointmentTypeName", apt.getAppointmentType() == 1 ? "日间巡诊" : "夜间急诊");
        detail.put("symptoms", apt.getSymptoms());
        detail.put("diagnosis", apt.getDiagnosis());
        detail.put("prescription", apt.getPrescription());
        detail.put("status", apt.getStatus());
        detail.put("statusName", getStatusName(apt.getStatus()));
        detail.put("queueNumber", apt.getQueueNumber());
        detail.put("address", apt.getAddress());
        detail.put("visitTime", apt.getVisitTime() != null ? apt.getVisitTime().toString() : null);
        detail.put("createTime", apt.getCreateTime() != null ? apt.getCreateTime().toString().replace("T", " ") : null);

        // 获取医生信息
        if (apt.getDoctorId() != null) {
            MedicalDoctor doctor = medicalDoctorMapper.selectById(apt.getDoctorId());
            if (doctor != null) {
                detail.put("doctorName", doctor.getName());
                detail.put("doctorTitle", doctor.getTitle());
                detail.put("doctorSpecialty", doctor.getSpecialty());
            }
        }

        return Result.success(detail);
    }
}
