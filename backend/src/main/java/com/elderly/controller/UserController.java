package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.entity.Family;
import com.elderly.entity.User;
import com.elderly.mapper.FamilyMapper;
import com.elderly.service.AdminLogService;
import com.elderly.service.UserService;
import com.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AdminLogService adminLogService;
    private final JwtUtil jwtUtil;
    private final FamilyMapper familyMapper;
    
    private void log(String token, String description) {
        try {
            if (token == null) return;
            Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
            User user = userService.getById(userId);
            if (user != null && user.getUserType() == 4) {
                adminLogService.log(userId, user.getName(), "用户管理", description);
            }
        } catch (Exception ignored) {}
    }

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
    public Result<Void> addUser(@RequestBody Map<String, Object> body,
                                @RequestHeader(value = "Authorization", required = false) String token) {
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
        
        // 全局手机号唯一性校验（不区分用户类型）
        User existUser = userService.getByPhone(phone);
        if (existUser != null) {
            return Result.error("该手机号已被注册");
        }
        
        // 如果有关联身份证号，使用带关联的注册方法
        if (relatedIdCard != null && !relatedIdCard.isEmpty()) {
            userService.registerWithRelation(phone, password, userType, name, idCard, workerType, relatedIdCard, address);
        } else {
            userService.register(phone, password, userType, name, idCard, workerType);
            
            // 如果是老人且有地址，自动创建family记录并关联
            if (userType == 1 && address != null && !address.isEmpty()) {
                User newUser = userService.getByPhoneAndType(phone, userType);
                if (newUser != null && newUser.getFamilyId() == null) {
                    Family family = new Family();
                    family.setAddress(address);
                    // 从地址中提取楼栋信息
                    String building = address.contains(" ") ? address.split(" ")[0] : address;
                    family.setBuilding(building);
                    familyMapper.insert(family);
                    
                    newUser.setFamilyId(family.getId());
                    newUser.setAddress(address);
                    userService.updateById(newUser);
                }
            }
        }
        
        String[] userTypeNames = {"", "老人", "子女", "服务人员", "管理员"};
        String typeName = userType >= 1 && userType <= 4 ? userTypeNames[userType] : "用户";
        log(token, "新增" + typeName + ": " + name + " (" + phone + ")");
        return Result.success();
    }
    
    /** 检查手机号是否已存在 */
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhoneExists(@RequestParam String phone) {
        User existUser = userService.getByPhone(phone);
        return Result.success(existUser != null);
    }
    
    /** 更新用户（管理员操作） */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user,
                                   @RequestHeader(value = "Authorization", required = false) String token) {
        user.setId(id);
        user.setPassword(null); // 不通过此接口修改密码
        // 如果不是服务人员，清空workerType
        User existUser = userService.getById(id);
        if (existUser != null && existUser.getUserType() != 3) {
            user.setWorkerType(null);
        }
        userService.updateById(user);
        
        // 同步更新family表的building字段（用于餐饮订单自动分配配送员）
        // 仅对老人用户(userType=1)处理
        if (user.getAddress() != null && existUser != null && existUser.getUserType() == 1) {
            String address = user.getAddress();
            String building = address.contains(" ") ? address.split(" ")[0] : address;
            
            if (existUser.getFamilyId() != null) {
                // 已有family记录，更新
                Family family = familyMapper.selectById(existUser.getFamilyId());
                if (family != null) {
                    family.setBuilding(building);
                    family.setAddress(address);
                    familyMapper.updateById(family);
                }
            } else {
                // 没有family记录，创建新的
                Family family = new Family();
                family.setAddress(address);
                family.setBuilding(building);
                familyMapper.insert(family);
                
                // 更新用户的familyId
                User updateUser = new User();
                updateUser.setId(id);
                updateUser.setFamilyId(family.getId());
                userService.updateById(updateUser);
            }
        }
        
        log(token, "修改用户: " + (existUser != null ? existUser.getName() : "ID=" + id));
        return Result.success();
    }
    
    /** 重置用户密码（管理员操作，默认重置为身份证后6位） */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @RequestHeader(value = "Authorization", required = false) String token) {
        String newPassword = body.get("newPassword");
        User user = userService.getById(id);
        if (newPassword == null || newPassword.isEmpty()) {
            // 默认重置为身份证后6位
            if (user != null && user.getIdCard() != null && user.getIdCard().length() >= 6) {
                newPassword = user.getIdCard().substring(user.getIdCard().length() - 6);
            } else {
                newPassword = "123456"; // 兜底默认密码
            }
        }
        userService.resetPassword(id, newPassword);
        log(token, "重置用户密码: " + (user != null ? user.getName() : "ID=" + id));
        return Result.success();
    }
    
    /** 禁用/启用用户 */
    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body,
                                     @RequestHeader(value = "Authorization", required = false) String token) {
        Integer status = body.get("status");
        User existUser = userService.getById(id);
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userService.updateById(user);
        String action = status == 1 ? "启用" : "禁用";
        log(token, action + "用户: " + (existUser != null ? existUser.getName() : "ID=" + id));
        return Result.success();
    }
    
    /** 删除用户 */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id,
                                   @RequestHeader(value = "Authorization", required = false) String token) {
        User user = userService.getById(id);
        userService.removeById(id);
        log(token, "删除用户: " + (user != null ? user.getName() : "ID=" + id));
        return Result.success();
    }
}
