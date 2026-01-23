package com.elderly.controller;

import com.elderly.common.Result;
import com.elderly.dto.CleaningBookingRequest;
import com.elderly.dto.CleaningWorkerVO;
import com.elderly.entity.CleaningOrder;
import com.elderly.entity.CleaningService;
import com.elderly.service.CleaningBookingService;
import com.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cleaning")
@RequiredArgsConstructor
public class CleaningController {

    private final CleaningBookingService cleaningBookingService;
    private final JwtUtil jwtUtil;

    @GetMapping("/services")
    public Result<List<CleaningService>> getServiceList() {
        return Result.success(cleaningBookingService.getServiceList());
    }

    @GetMapping("/workers")
    public Result<List<CleaningWorkerVO>> getAvailableWorkers(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(cleaningBookingService.getAvailableWorkers(date));
    }

    @PostMapping("/order")
    public Result<Long> createOrder(@RequestBody CleaningBookingRequest request,
                                    @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(cleaningBookingService.createOrder(request, userId));
    }

    @PostMapping("/order/{id}/cancel")
    public Result<Map<String, Object>> cancelOrder(@PathVariable Long id,
                                                   @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(cleaningBookingService.cancelOrder(id, userId));
    }

    @GetMapping("/orders")
    public Result<List<CleaningOrder>> getUserOrders(
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(cleaningBookingService.getUserOrders(userId, status));
    }

    @PostMapping("/order/{id}/start")
    public Result<Boolean> startService(@PathVariable Long id,
                                        @RequestParam String serviceCode) {
        return Result.success(cleaningBookingService.startService(id, serviceCode));
    }

    @PostMapping("/order/{id}/complete")
    public Result<Boolean> completeService(@PathVariable Long id,
                                           @RequestParam(required = false) String evidence) {
        return Result.success(cleaningBookingService.completeService(id, evidence));
    }

    /**
     * 用户确认服务完成
     */
    @PostMapping("/order/{id}/confirm")
    public Result<Boolean> confirmService(@PathVariable Long id,
                                          @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(cleaningBookingService.confirmService(id, userId));
    }

    /**
     * 用户提交争议
     */
    @PostMapping("/order/{id}/dispute")
    public Result<Boolean> disputeService(@PathVariable Long id,
                                          @RequestParam String reason,
                                          @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(cleaningBookingService.disputeService(id, userId, reason));
    }
}
