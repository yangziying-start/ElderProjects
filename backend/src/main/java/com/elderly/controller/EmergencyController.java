package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.common.Result;
import com.elderly.common.annotation.Log;
import com.elderly.entity.EmergencyCall;
import com.elderly.entity.User;
import com.elderly.service.AdminLogService;
import com.elderly.service.EmergencyService;
import com.elderly.service.UserService;
import com.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;
    private final AdminLogService adminLogService;
    private final UserService userService;
    private final JwtUtil jwtUtil;


    @PostMapping("/call")
    public Result<Long> triggerCall(@RequestAttribute Long userId, @RequestBody Map<String, Integer> body) {
        Integer eventType = body.get("eventType");
        return Result.success(emergencyService.triggerCall(userId, eventType));
    }

    @PostMapping("/{id}/respond")
    public Result<Boolean> respond(@PathVariable Long id, @RequestAttribute Long userId) {
        return Result.success(emergencyService.respond(id, userId));
    }

    @PostMapping("/{id}/complete")
    public Result<Boolean> complete(@PathVariable Long id, @RequestParam String result) {
        return Result.success(emergencyService.complete(id, result));
    }

    /** 管理后台标记已处理 */
    @PostMapping("/{id}/process")
    @Log(module = "应急管理", operationType = 2, desc = "'处理应急呼叫 ID: ' + #id")
    public Result<Boolean> process(@PathVariable Long id) {
        boolean result = emergencyService.process(id);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<List<EmergencyCall>> list(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<EmergencyCall> wrapper = new LambdaQueryWrapper<EmergencyCall>()
                .eq(status != null, EmergencyCall::getStatus, status)
                .orderByDesc(EmergencyCall::getTriggerTime);
        return Result.success(emergencyService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<EmergencyCall> getById(@PathVariable Long id) {
        return Result.success(emergencyService.getById(id));
    }
}
