package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.entity.AdminLog;
import com.elderly.entity.User;
import com.elderly.service.AdminLogService;
import com.elderly.service.UserService;
import com.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final AdminLogService adminLogService;
    private final JwtUtil jwtUtil;

    /**
     * 获取管理员列表
     */
    @GetMapping("/admins")
    public Result<List<User>> getAdminList(@RequestHeader("Authorization") String token) {
        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        User currentUser = userService.getById(currentUserId);
        
        // 只有超级管理员可以查看管理员列表
        if (currentUser == null || currentUser.getUserType() != 4) {
            throw new RuntimeException("无权限访问");
        }
        
        List<User> admins = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getUserType, 4)
                .eq(User::getDeleted, 0)
                .orderByAsc(User::getAdminRole)
                .orderByDesc(User::getCreateTime));
        
        return Result.success(admins);
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentAdmin(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        User user = userService.getById(userId);
        if (user == null || user.getUserType() != 4) {
            throw new RuntimeException("非管理员用户");
        }
        return Result.success(user);
    }

    /**
     * 新增管理员（仅超级管理员可操作）
     */
    @PostMapping("/admin")
    public Result<Long> addAdmin(@RequestBody Map<String, Object> body,
                                 @RequestHeader("Authorization") String token) {
        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        User currentUser = userService.getById(currentUserId);
        
        // 只有超级管理员可以新增管理员
        if (currentUser == null || currentUser.getUserType() != 4 || currentUser.getAdminRole() != 1) {
            throw new RuntimeException("只有超级管理员可以新增管理员");
        }
        
        String phone = (String) body.get("phone");
        String name = (String) body.get("name");
        String password = (String) body.get("password");
        Integer adminRole = body.get("adminRole") != null ? (Integer) body.get("adminRole") : 2;
        
        if (phone == null || name == null || password == null) {
            throw new RuntimeException("手机号、姓名、密码不能为空");
        }
        
        // 检查手机号是否已存在
        User existing = userService.getByPhone(phone);
        if (existing != null) {
            throw new RuntimeException("该手机号已被注册");
        }
        
        // 不允许创建超级管理员
        if (adminRole == 1) {
            throw new RuntimeException("不能创建超级管理员");
        }
        
        User admin = new User();
        admin.setPhone(phone);
        admin.setName(name);
        admin.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        admin.setUserType(4);
        admin.setAdminRole(adminRole);
        admin.setStatus(1);
        admin.setDeleted(0);
        
        userService.save(admin);
        
        // 记录日志
        adminLogService.log(currentUserId, currentUser.getName(), "管理员管理", "新增管理员: " + name);
        
        return Result.success(admin.getId());
    }

    /**
     * 修改管理员信息（仅超级管理员可操作）
     */
    @PutMapping("/admin/{id}")
    public Result<Boolean> updateAdmin(@PathVariable Long id,
                                       @RequestBody Map<String, Object> body,
                                       @RequestHeader("Authorization") String token) {
        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        User currentUser = userService.getById(currentUserId);
        
        // 只有超级管理员可以修改管理员
        if (currentUser == null || currentUser.getUserType() != 4 || currentUser.getAdminRole() != 1) {
            throw new RuntimeException("只有超级管理员可以修改管理员信息");
        }
        
        User admin = userService.getById(id);
        if (admin == null || admin.getUserType() != 4) {
            throw new RuntimeException("管理员不存在");
        }
        
        // 不能修改超级管理员
        if (admin.getAdminRole() == 1 && !admin.getId().equals(currentUserId)) {
            throw new RuntimeException("不能修改其他超级管理员");
        }
        
        String name = (String) body.get("name");
        if (name != null) {
            admin.setName(name);
        }
        
        userService.updateById(admin);
        
        // 记录日志
        adminLogService.log(currentUserId, currentUser.getName(), "管理员管理", "修改管理员: " + admin.getName());
        
        return Result.success(true);
    }

    /**
     * 重置管理员密码（仅超级管理员可操作）
     */
    @PostMapping("/admin/{id}/reset-password")
    public Result<Boolean> resetAdminPassword(@PathVariable Long id,
                                              @RequestBody Map<String, String> body,
                                              @RequestHeader("Authorization") String token) {
        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        User currentUser = userService.getById(currentUserId);
        
        // 只有超级管理员可以重置密码
        if (currentUser == null || currentUser.getUserType() != 4 || currentUser.getAdminRole() != 1) {
            throw new RuntimeException("只有超级管理员可以重置密码");
        }
        
        User admin = userService.getById(id);
        if (admin == null || admin.getUserType() != 4) {
            throw new RuntimeException("管理员不存在");
        }
        
        // 不能重置超级管理员密码（除非是自己）
        if (admin.getAdminRole() == 1 && !admin.getId().equals(currentUserId)) {
            throw new RuntimeException("不能重置其他超级管理员的密码");
        }
        
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 6) {
            newPassword = "123456";
        }
        
        admin.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        userService.updateById(admin);
        
        // 记录日志
        adminLogService.log(currentUserId, currentUser.getName(), "管理员管理", "重置管理员密码: " + admin.getName());
        
        return Result.success(true);
    }

    /**
     * 禁用/启用管理员（仅超级管理员可操作）
     */
    @PostMapping("/admin/{id}/status")
    public Result<Boolean> toggleAdminStatus(@PathVariable Long id,
                                             @RequestBody Map<String, Integer> body,
                                             @RequestHeader("Authorization") String token) {
        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        User currentUser = userService.getById(currentUserId);
        
        // 只有超级管理员可以操作
        if (currentUser == null || currentUser.getUserType() != 4 || currentUser.getAdminRole() != 1) {
            throw new RuntimeException("只有超级管理员可以操作");
        }
        
        User admin = userService.getById(id);
        if (admin == null || admin.getUserType() != 4) {
            throw new RuntimeException("管理员不存在");
        }
        
        // 不能禁用超级管理员
        if (admin.getAdminRole() == 1) {
            throw new RuntimeException("不能禁用超级管理员");
        }
        
        // 不能禁用自己
        if (admin.getId().equals(currentUserId)) {
            throw new RuntimeException("不能禁用自己");
        }
        
        Integer status = body.get("status");
        admin.setStatus(status != null ? status : (admin.getStatus() == 1 ? 0 : 1));
        userService.updateById(admin);
        
        // 记录日志
        String action = admin.getStatus() == 1 ? "启用" : "禁用";
        adminLogService.log(currentUserId, currentUser.getName(), "管理员管理", action + "管理员: " + admin.getName());
        
        return Result.success(true);
    }

    /**
     * 删除管理员（仅超级管理员可操作）
     */
    @DeleteMapping("/admin/{id}")
    public Result<Boolean> deleteAdmin(@PathVariable Long id,
                                       @RequestHeader("Authorization") String token) {
        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        User currentUser = userService.getById(currentUserId);
        
        // 只有超级管理员可以删除
        if (currentUser == null || currentUser.getUserType() != 4 || currentUser.getAdminRole() != 1) {
            throw new RuntimeException("只有超级管理员可以删除管理员");
        }
        
        User admin = userService.getById(id);
        if (admin == null || admin.getUserType() != 4) {
            throw new RuntimeException("管理员不存在");
        }
        
        // 不能删除超级管理员
        if (admin.getAdminRole() == 1) {
            throw new RuntimeException("不能删除超级管理员");
        }
        
        // 不能删除自己
        if (admin.getId().equals(currentUserId)) {
            throw new RuntimeException("不能删除自己");
        }
        
        userService.removeById(id);
        
        // 记录日志
        adminLogService.log(currentUserId, currentUser.getName(), "管理员管理", "删除管理员: " + admin.getName());
        
        return Result.success(true);
    }

    /**
     * 获取操作日志列表
     */
    @GetMapping("/logs")
    public Result<Page<AdminLog>> getLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer operationType,
            @RequestHeader("Authorization") String token) {
        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        User currentUser = userService.getById(currentUserId);
        
        // 只有管理员可以查看日志
        if (currentUser == null || currentUser.getUserType() != 4) {
            throw new RuntimeException("无权限访问");
        }
        
        // 普通管理员只能查看自己的日志
        if (currentUser.getAdminRole() != 1) {
            adminId = currentUserId;
        }
        
        return Result.success(adminLogService.pageQuery(page, size, adminId, module, operationType));
    }
}
