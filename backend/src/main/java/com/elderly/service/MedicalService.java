package com.elderly.service;

import com.elderly.dto.DoctorListVO;
import com.elderly.dto.MedicalBookingRequest;
import com.elderly.entity.MedicalAppointment;
import java.time.LocalDate;
import java.util.List;

public interface MedicalService {
    
    /**
     * 获取可预约医生列表(含熔断状态)
     */
    List<DoctorListVO> getAvailableDoctors(LocalDate date);
    
    /**
     * 检查医生是否可预约(熔断机制)
     */
    boolean checkDoctorAvailable(Long doctorId, LocalDate date);
    
    /**
     * 创建医疗巡诊预约
     */
    Long createAppointment(MedicalBookingRequest request, Long userId);
    
    /**
     * 取消预约
     */
    boolean cancelAppointment(Long appointmentId, Long userId);
    
    /**
     * 获取用户预约列表
     */
    List<MedicalAppointment> getUserAppointments(Long userId, Integer status);
    
    /**
     * 更新熔断状态(定时任务调用)
     */
    void updateCircuitBreakerStatus(Long doctorId);
    
    /**
     * 开始巡诊
     */
    boolean startVisit(Long appointmentId, Long doctorId);
    
    /**
     * 完成巡诊
     */
    boolean completeVisit(Long appointmentId, String diagnosis, String prescription);
    
    /**
     * 用户确认订单完成
     */
    boolean confirmAppointment(Long appointmentId, Long userId);
    
    /**
     * 用户提交争议
     */
    boolean disputeAppointment(Long appointmentId, Long userId, String reason);
    
    /**
     * 管理员处理争议订单
     */
    boolean resolveDispute(Long appointmentId);
}
