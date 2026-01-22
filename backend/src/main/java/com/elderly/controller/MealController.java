package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.common.Result;
import com.elderly.dto.MealBookingRequest;
import com.elderly.dto.WeeklyMenuVO;
import com.elderly.entity.MealOrder;
import com.elderly.entity.User;
import com.elderly.mapper.UserMapper;
import com.elderly.service.MealService;
import com.elderly.service.PointsService;
import com.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meal")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;
    private final PointsService pointsService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @GetMapping("/menu")
    public Result<WeeklyMenuVO> getWeeklyMenu(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (date == null) date = LocalDate.now();
        return Result.success(mealService.getWeeklyMenu(date));
    }

    @GetMapping("/check-window")
    public Result<Map<String, Object>> checkBookingWindow(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Integer mealType) {
        return Result.success(mealService.checkBookingWindow(date, mealType));
    }

    @PostMapping("/order")
    public Result<Long> createOrder(@RequestBody MealBookingRequest request,
                                    @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(mealService.createOrder(request, userId));
    }

    @PostMapping("/order/{id}/cancel")
    public Result<Map<String, Object>> cancelOrder(@PathVariable Long id,
                                                   @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(mealService.cancelOrder(id, userId));
    }

    @GetMapping("/orders")
    public Result<List<MealOrder>> getUserOrders(
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(mealService.getUserOrders(userId, status));
    }

    @GetMapping("/points")
    public Result<?> getUserPoints(
            @RequestParam(required = false) Long elderlyId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        // 如果指定了老人ID，则查询老人的积分（用于子女代预约场景）
        Long targetUserId = elderlyId != null ? elderlyId : userId;
        return Result.success(pointsService.getUserPoints(targetUserId));
    }

    /**
     * 获取配送员列表（用于餐饮配送选择）
     */
    @GetMapping("/workers")
    public Result<List<Map<String, Object>>> getWorkers() {
        // 获取所有配送员（userType=3 且 workerType=1）
        List<User> workers = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUserType, 3)
                        .eq(User::getWorkerType, 1)  // 1-配送员
                        .eq(User::getStatus, 1));
        
        List<Map<String, Object>> result = workers.stream().map(worker -> {
            Map<String, Object> map = new HashMap<>();
            map.put("workerId", worker.getId());
            map.put("workerName", worker.getName());
            map.put("avatar", worker.getAvatar());
            map.put("phone", worker.getPhone());
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }
}
