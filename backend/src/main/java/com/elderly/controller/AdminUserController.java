package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.common.annotation.Log;
import com.elderly.entity.AdminLog;
import com.elderly.entity.Family;
import com.elderly.entity.User;
import com.elderly.mapper.FamilyMapper;
import com.elderly.service.AdminLogService;
import com.elderly.service.UserService;
import com.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.alibaba.excel.EasyExcel;
import com.elderly.dto.UserImportDTO;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays; // 需要用到 Arrays.asList 或 List.of

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final AdminLogService adminLogService;
    private final JwtUtil jwtUtil;
    private final FamilyMapper familyMapper;

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
//                .eq(User::getDeleted, 0)
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
    @Log(module = "管理员管理", operationType = 1, desc = "'新增管理员: ' + #body['name']")
    public Result<Long> addAdmin(@RequestBody Map<String, Object> body) {
//        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
//        User currentUser = userService.getById(currentUserId);
//
//        // 只有超级管理员可以新增管理员
//        if (currentUser == null || currentUser.getUserType() != 4 || currentUser.getAdminRole() != 1) {
//            throw new RuntimeException("只有超级管理员可以新增管理员");
//        }
        
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
//        admin.setDeleted(0);
        
        userService.save(admin);
        return Result.success(admin.getId());
    }

    /**
     * 修改管理员信息（仅超级管理员可操作）
     */
    @PutMapping("/admin/{id}")
    @Log(module = "管理员管理", operationType = 2, desc = "'修改管理员信息 ID: ' + #id")
    public Result<Boolean> updateAdmin(@PathVariable Long id,
                                       @RequestBody Map<String, Object> body) {
//        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
//        User currentUser = userService.getById(currentUserId);
//
//        // 只有超级管理员可以修改管理员
//        if (currentUser == null || currentUser.getUserType() != 4 || currentUser.getAdminRole() != 1) {
//            throw new RuntimeException("只有超级管理员可以修改管理员信息");
//        }
        
        User admin = userService.getById(id);
        if (admin == null || admin.getUserType() != 4) {
            throw new RuntimeException("管理员不存在");
        }
        
//        // 不能修改超级管理员
//        if (admin.getAdminRole() == 1 && !admin.getId().equals(currentUserId)) {
//            throw new RuntimeException("不能修改其他超级管理员");
//        }
        
        String name = (String) body.get("name");
        if (name != null) {
            admin.setName(name);
        }
        userService.updateById(admin);
        return Result.success(true);
    }

    /**
     * 重置管理员密码（仅超级管理员可操作）
     */
    @PostMapping("/admin/{id}/reset-password")
    @Log(module = "管理员管理", operationType = 2, desc = "'重置管理员密码 ID: ' + #id")
    public Result<Boolean> resetAdminPassword(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
//        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
//        User currentUser = userService.getById(currentUserId);
//
//        // 只有超级管理员可以重置密码
//        if (currentUser == null || currentUser.getUserType() != 4 || currentUser.getAdminRole() != 1) {
//            throw new RuntimeException("只有超级管理员可以重置密码");
//        }
        
        User admin = userService.getById(id);
        if (admin == null || admin.getUserType() != 4) {
            throw new RuntimeException("管理员不存在");
        }
        
//        // 不能重置超级管理员密码（除非是自己）
//        if (admin.getAdminRole() == 1 && !admin.getId().equals(currentUserId)) {
//            throw new RuntimeException("不能重置其他超级管理员的密码");
//        }
        
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 6) {
            newPassword = "123456";
        }
        
        admin.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        userService.updateById(admin);
        
        return Result.success(true);
    }

    /**
     * 禁用/启用管理员（仅超级管理员可操作）
     */
    @PostMapping("/admin/{id}/status")
    @Log(module = "管理员管理", operationType = 2, desc = "'修改管理员状态 ID: ' + #id + ' 状态: ' + (#body['status']==1?'启用':'禁用')")
    public Result<Boolean> toggleAdminStatus(@PathVariable Long id,
                                             @RequestBody Map<String, Integer> body) {
//        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
//        User currentUser = userService.getById(currentUserId);
//
//        // 只有超级管理员可以操作
//        if (currentUser == null || currentUser.getUserType() != 4 || currentUser.getAdminRole() != 1) {
//            throw new RuntimeException("只有超级管理员可以操作");
//        }
        
        User admin = userService.getById(id);
        if (admin == null || admin.getUserType() != 4) {
            throw new RuntimeException("管理员不存在");
        }
        
        // 不能禁用超级管理员
        if (admin.getAdminRole() == 1) {
            throw new RuntimeException("不能禁用超级管理员");
        }
        
//        // 不能禁用自己
//        if (admin.getId().equals(currentUserId)) {
//            throw new RuntimeException("不能禁用自己");
//        }
        
        Integer status = body.get("status");
        admin.setStatus(status != null ? status : (admin.getStatus() == 1 ? 0 : 1));
        userService.updateById(admin);
        return Result.success(true);
    }

    /**
     * 删除管理员（仅超级管理员可操作）
     */
    @DeleteMapping("/admin/{id}")
    @Log(module = "管理员管理", operationType = 3, desc = "'删除管理员 ID: ' + #id")
    public Result<Boolean> deleteAdmin(@PathVariable Long id) {
//        Long currentUserId = jwtUtil.getUserId(token.replace("Bearer ", ""));
//        User currentUser = userService.getById(currentUserId);
//
//        // 只有超级管理员可以删除
//        if (currentUser == null || currentUser.getUserType() != 4 || currentUser.getAdminRole() != 1) {
//            throw new RuntimeException("只有超级管理员可以删除管理员");
//        }
        
        User admin = userService.getById(id);
        if (admin == null || admin.getUserType() != 4) {
            throw new RuntimeException("管理员不存在");
        }
        
        // 不能删除超级管理员
        if (admin.getAdminRole() == 1) {
            throw new RuntimeException("不能删除超级管理员");
        }
        
//        // 不能删除自己
//        if (admin.getId().equals(currentUserId)) {
//            throw new RuntimeException("不能删除自己");
//        }
        
        userService.removeById(id);
        
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


    /**
     * 批量导入用户 (Excel)
     * 特性：模板校验、数据预校验（报错即中止）、自动跳过已存在、多身份证关联绑定、老人子女地址必填
     */
    @PostMapping("/import")
    @Transactional(rollbackFor = Exception.class)
    @Log(module = "用户管理", operationType = 1, desc = "'批量导入用户'")
    public Result<String> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. 读取 Excel 数据
        List<UserImportDTO> list;
        try {
            list = EasyExcel.read(file.getInputStream())
                    .head(UserImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            return Result.error("文件解析失败，请检查文件格式是否正确");
        }

        if (list.isEmpty()) return Result.error("文件内容为空");

        // 2. 模板校验
        UserImportDTO firstRow = list.get(0);
        if (firstRow.getName() == null && firstRow.getPhone() == null && firstRow.getUserTypeStr() == null) {
            return Result.error("导入失败：表格模板错误！请下载最新模板并严格按照表头填写。");
        }

        // 3. 数据完整性预校验（发现格式错误直接中止）
        List<String> validationErrors = new ArrayList<>();

        List<String> validUserTypes = Arrays.asList("老人", "子女", "服务人员");
        List<String> validWorkerTypes = Arrays.asList("配送员", "保洁员", "医疗人员");

        for (int i = 0; i < list.size(); i++) {
            UserImportDTO dto = list.get(i);
            int rowNum = i + 2;
            StringBuilder rowError = new StringBuilder();

            // 3.1 基础必填项校验
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                rowError.append("姓名缺失; ");
            }
            if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
                rowError.append("手机号缺失; ");
            } else if (!dto.getPhone().matches("^1\\d{10}$")) {
                rowError.append("手机号格式错误; ");
            }
            if (dto.getIdCard() == null || dto.getIdCard().trim().isEmpty()) {
                rowError.append("身份证号缺失; ");
            } else if (dto.getIdCard().length() != 18) {
                rowError.append("身份证号长度应为18位; ");
            }
            if (dto.getUserTypeStr() == null || dto.getUserTypeStr().trim().isEmpty()) {
                rowError.append("用户类型缺失; ");
            } else if (!validUserTypes.contains(dto.getUserTypeStr())) {
                rowError.append("用户类型必须为：老人/子女/服务人员; ");
            }

            // 3.2 【新增需求】老人和子女，地址必填
            if ("老人".equals(dto.getUserTypeStr()) || "子女".equals(dto.getUserTypeStr())) {
                if (dto.getAddress() == null || dto.getAddress().trim().isEmpty()) {
                    rowError.append("老人/子女必须填写地址; ");
                }
            }

            // 3.3 服务人员类型校验
            if ("服务人员".equals(dto.getUserTypeStr())) {
                if (dto.getWorkerTypeStr() == null || dto.getWorkerTypeStr().trim().isEmpty()) {
                    rowError.append("服务人员类型缺失; ");
                } else if (!validWorkerTypes.contains(dto.getWorkerTypeStr())) {
                    rowError.append("服务人员类型无效; ");
                }
            }

            if (rowError.length() > 0) {
                validationErrors.add("第" + rowNum + "行：" + rowError.toString());
            }
        }

        // 如果存在校验错误，中止导入
        if (!validationErrors.isEmpty()) {
            String errorMsg = "导入中止，发现数据格式错误（未执行任何写入）：\n" + String.join("\n", validationErrors);
            return Result.error(errorMsg);
        }

        // 4. 校验通过，开始执行业务逻辑
        int successCount = 0;
        List<String> skippedUsers = new ArrayList<>();
        List<String> dbErrors = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            UserImportDTO dto = list.get(i);
            int rowNum = i + 2;

            // 4.1 查重跳过
            User existUser = userService.getByPhone(dto.getPhone());
            if (existUser != null) {
                skippedUsers.add(dto.getName() + "(" + dto.getPhone() + ")");
                continue;
            }

            // 4.2 数据转换
            Integer userType = 1;
            if ("子女".equals(dto.getUserTypeStr())) userType = 2;
            else if ("服务人员".equals(dto.getUserTypeStr())) userType = 3;

            Integer workerType = null;
            if (userType == 3) {
                if ("配送员".equals(dto.getWorkerTypeStr())) workerType = 1;
                else if ("保洁员".equals(dto.getWorkerTypeStr())) workerType = 2;
                else if ("医疗人员".equals(dto.getWorkerTypeStr())) workerType = 3;
            }

            String password = "123456";
            if (dto.getIdCard() != null && dto.getIdCard().length() >= 6) {
                password = dto.getIdCard().substring(dto.getIdCard().length() - 6);
            }

            try {
                // 4.3 构建对象
                User user = new User();
                user.setPhone(dto.getPhone());
                user.setName(dto.getName());
                user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
                user.setIdCard(dto.getIdCard());
                user.setUserType(userType);
                user.setWorkerType(workerType);
                user.setAddress(dto.getAddress());
                user.setRelatedIdCard(dto.getRelatedIdCard());
                user.setStatus(1);


                userService.save(user);

                // 4.4 处理家庭绑定
                Long finalFamilyId = null;

                if ((userType == 1 || userType == 2) &&
                        dto.getRelatedIdCard() != null && !dto.getRelatedIdCard().isEmpty()) {

                    String[] relatedIds = dto.getRelatedIdCard().replace("，", ",").split(",");

                    for (String relatedId : relatedIds) {
                        String cleanId = relatedId.trim();
                        if (cleanId.isEmpty()) continue;

                        User targetUser = userService.getOne(new LambdaQueryWrapper<User>()
                                .eq(User::getIdCard, cleanId));

                        if (targetUser != null) {
                            if (finalFamilyId == null) {
                                if (targetUser.getFamilyId() != null) {
                                    finalFamilyId = targetUser.getFamilyId();
                                } else {
                                    Family family = new Family();
                                    String addr = (userType == 1) ? dto.getAddress() : targetUser.getAddress();
                                    family.setAddress(addr);
                                    if (addr != null && !addr.isEmpty()) family.setBuilding(addr.split(" ")[0]);
                                    familyMapper.insert(family);
                                    finalFamilyId = family.getId();

                                    targetUser.setFamilyId(finalFamilyId);
                                    userService.updateById(targetUser);
                                }
                            } else {
                                if (targetUser.getFamilyId() == null) {
                                    targetUser.setFamilyId(finalFamilyId);
                                    userService.updateById(targetUser);
                                }
                            }
                        }
                    }
                }

                // 自动建家（单人户）
                if (finalFamilyId == null && userType == 1 && dto.getAddress() != null && !dto.getAddress().isEmpty()) {
                    Family family = new Family();
                    family.setAddress(dto.getAddress());
                    if (dto.getAddress() != null) family.setBuilding(dto.getAddress().split(" ")[0]);
                    familyMapper.insert(family);
                    finalFamilyId = family.getId();
                }

                if (finalFamilyId != null) {
                    user.setFamilyId(finalFamilyId);
                    userService.updateById(user);
                }

                successCount++;

            } catch (Exception e) {
                if (e.getMessage().contains("Duplicate entry")) {
                    skippedUsers.add(dto.getName() + "（账号已存在）");
                } else {
                    dbErrors.add("第" + rowNum + "行入库失败：" + e.getMessage());
                }
            }
        }

        StringBuilder msg = new StringBuilder();
        msg.append("导入操作完成。成功插入：").append(successCount).append(" 条。");

        if (!skippedUsers.isEmpty()) {
            msg.append("\n已自动跳过已存在的账号（").append(skippedUsers.size()).append("条）：")
                    .append(String.join("，", skippedUsers));
        }

        if (!dbErrors.isEmpty()) {
            msg.append("\n部分数据写入失败（").append(dbErrors.size()).append("条）：")
                    .append(String.join("；", dbErrors));
            return Result.success(msg.toString());
        }

        return Result.success(msg.toString());
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @Log(module = "用户管理", operationType = 3, desc = "'批量删除用户 ID: ' + #ids")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Result.error("请选择要删除的用户");
        userService.removeByIds(ids);
        return Result.success();
    }

    /**
     * 批量修改状态
     */
    @PutMapping("/batch/status")
    @Log(module = "用户管理", operationType = 2, desc = "'批量修改状态 IDs: ' + #body['ids'] + ' 状态: ' + #body['status']")
    public Result<Void> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        List<Long> ids = (List<Long>) body.get("ids"); // 注意这里可能需要类型强转处理
        Integer status = (Integer) body.get("status");

        if (ids == null || ids.isEmpty()) return Result.error("请选择用户");

        // MyBatis-Plus 批量更新
        User user = new User();
        user.setStatus(status);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().in(User::getId, ids);
        userService.update(user, wrapper);

        return Result.success();
    }
}
