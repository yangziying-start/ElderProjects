package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.entity.User;
import com.elderly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 小程序手机号密码登录 */
    @PostMapping("/login")
    public Result<String> login(@RequestBody Map<String, Object> body) {
        String phone = (String) body.get("phone");
        String password = (String) body.get("password");
        Integer userType = body.get("userType") != null ? 
                Integer.parseInt(body.get("userType").toString()) : 1;
        
        String token = userService.loginByPhone(phone, password, userType);
        return Result.success(token);
    }
    
    /** 小程序注册 */
    @PostMapping("/register")
    public Result<String> register(@RequestBody Map<String, Object> body) {
        String phone = (String) body.get("phone");
        String password = (String) body.get("password");
        String name = (String) body.get("name");
        String idCard = (String) body.get("idCard");
        Integer userType = body.get("userType") != null ? 
                Integer.parseInt(body.get("userType").toString()) : 1;
        
        String token = userService.register(phone, password, userType, name, idCard);
        return Result.success(token);
    }

    /** 管理后台账号密码登录 */
    @PostMapping("/admin/login")
    public Result<String> adminLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String token = userService.adminLogin(username, password);
        return Result.success(token);
    }

    /** 获取当前用户信息 */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute Long userId) {
        return Result.success(userService.getById(userId));
    }

    /** 更新用户信息 */
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestAttribute Long userId, @RequestBody User user) {
        user.setId(userId);
        user.setPassword(null); // 不允许通过此接口修改密码
        userService.updateById(user);
        return Result.success();
    }
    
    /** 修改密码 */
    @PostMapping("/password")
    public Result<Void> updatePassword(@RequestAttribute Long userId, @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success();
    }
    
    // ========== 管理后台接口 ==========
    
    /** 分页查询用户列表 */
    @GetMapping("/page")
    public Result<Page<User>> pageUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer workerType,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String name) {
        
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(userType != null, User::getUserType, userType)
                .eq(workerType != null, User::getWorkerType, workerType)
                .like(phone != null, User::getPhone, phone)
                .like(name != null, User::getName, name)
                .orderByDesc(User::getCreateTime);
        return Result.success(userService.page(pageParam, wrapper));
    }
    
    /** 获取用户详情 */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
    
    /** 新增用户（管理员操作） */
    @PostMapping
    public Result<Void> addUser(@RequestBody Map<String, Object> body) {
        String phone = (String) body.get("phone");
        String password = (String) body.get("password");
        String name = (String) body.get("name");
        String idCard = (String) body.get("idCard");
        String relatedIdCard = (String) body.get("relatedIdCard");
        String address = (String) body.get("address");
        Integer userType = body.get("userType") != null ? 
                Integer.parseInt(body.get("userType").toString()) : 1;
        Integer workerType = body.get("workerType") != null ? 
                Integer.parseInt(body.get("workerType").toString()) : null;
        
        // 如果有关联身份证号，使用带关联的注册方法
        if (relatedIdCard != null && !relatedIdCard.isEmpty()) {
            userService.registerWithRelation(phone, password, userType, name, idCard, workerType, relatedIdCard, address);
        } else {
            userService.register(phone, password, userType, name, idCard, workerType);
        }
        return Result.success();
    }
    
    /** 更新用户（管理员操作） */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setPassword(null); // 不通过此接口修改密码
        // 如果不是服务人员，清空workerType
        User existUser = userService.getById(id);
        if (existUser != null && existUser.getUserType() != 3) {
            user.setWorkerType(null);
        }
        userService.updateById(user);
        return Result.success();
    }
    
    /** 重置用户密码（管理员操作，默认重置为身份证后6位） */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            // 默认重置为身份证后6位
            User user = userService.getById(id);
            if (user != null && user.getIdCard() != null && user.getIdCard().length() >= 6) {
                newPassword = user.getIdCard().substring(user.getIdCard().length() - 6);
            } else {
                newPassword = "123456"; // 兜底默认密码
            }
        }
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
    
    /** 禁用/启用用户 */
    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }
    
    /** 删除用户 */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}
