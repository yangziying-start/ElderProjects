package com.elderly.controller;

import com.elderly.common.Result;
import com.elderly.dto.DoctorListVO;
import com.elderly.dto.MedicalBookingRequest;
import com.elderly.entity.MedicalAppointment;
import com.elderly.service.MedicalService;
import com.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medical")
@RequiredArgsConstructor
public class MedicalController {

    private final MedicalService medicalService;
    private final JwtUtil jwtUtil;

    @GetMapping("/doctors")
    public Result<List<DoctorListVO>> getAvailableDoctors(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (date == null) date = LocalDate.now();
        return Result.success(medicalService.getAvailableDoctors(date));
    }

    @GetMapping("/check-available")
    public Result<Boolean> checkDoctorAvailable(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(medicalService.checkDoctorAvailable(doctorId, date));
    }

    @PostMapping("/appointment")
    public Result<Long> createAppointment(@RequestBody MedicalBookingRequest request,
                                          @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(medicalService.createAppointment(request, userId));
    }

    @PostMapping("/appointment/{id}/cancel")
    public Result<Boolean> cancelAppointment(@PathVariable Long id,
                                             @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(medicalService.cancelAppointment(id, userId));
    }

    @GetMapping("/appointments")
    public Result<List<MedicalAppointment>> getUserAppointments(
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(medicalService.getUserAppointments(userId, status));
    }

    @PostMapping("/appointment/{id}/start")
    public Result<Boolean> startVisit(@PathVariable Long id,
                                      @RequestParam Long doctorId) {
        return Result.success(medicalService.startVisit(id, doctorId));
    }

    @PostMapping("/appointment/{id}/complete")
    public Result<Boolean> completeVisit(@PathVariable Long id,
                                         @RequestParam String diagnosis,
                                         @RequestParam(required = false) String prescription) {
        return Result.success(medicalService.completeVisit(id, diagnosis, prescription));
    }
    
    /**
     * 用户确认预约完成
     */
    @PostMapping("/appointment/{id}/confirm")
    public Result<Boolean> confirmAppointment(@PathVariable Long id,
                                              @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(medicalService.confirmAppointment(id, userId));
    }
    
    /**
     * 用户提交争议
     */
    @PostMapping("/appointment/{id}/dispute")
    public Result<Boolean> disputeAppointment(@PathVariable Long id,
                                              @RequestParam String reason,
                                              @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(medicalService.disputeAppointment(id, userId, reason));
    }
}
