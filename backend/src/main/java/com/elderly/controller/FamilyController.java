package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.common.Result;
import com.elderly.entity.Family;
import com.elderly.entity.User;
import com.elderly.mapper.FamilyMapper;
import com.elderly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class FamilyController {

    private final UserService userService;
    private final FamilyMapper familyMapper;

    /**
     * 获取紧急联系人列表（老人端使用）
     * 返回同一家庭的子女作为紧急联系人
     */
    @GetMapping("/contacts")
    public Result<List<Map<String, Object>>> getContacts(@RequestAttribute Long userId) {
        User currentUser = userService.getById(userId);
        List<Map<String, Object>> contacts = new ArrayList<>();
        
        if (currentUser == null) {
            return Result.success(contacts);
        }
        
        // 如果有家庭ID，查询同一家庭的子女
        if (currentUser.getFamilyId() != null) {
            List<User> familyMembers = userService.list(new LambdaQueryWrapper<User>()
                    .eq(User::getFamilyId, currentUser.getFamilyId())
                    .eq(User::getUserType, 2) // 子女
                    .eq(User::getStatus, 1));
            
            for (User member : familyMembers) {
                Map<String, Object> contact = new HashMap<>();
                contact.put("id", member.getId());
                contact.put("name", member.getName());
                contact.put("phone", member.getPhone());
                contact.put("relation", "子女");
                contacts.add(contact);
            }
        }
        
        // 如果有关联身份证号但还没建立家庭关系，也尝试查找
        if (contacts.isEmpty() && currentUser.getRelatedIdCard() != null && !currentUser.getRelatedIdCard().isEmpty()) {
            User relatedUser = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getIdCard, currentUser.getRelatedIdCard())
                    .eq(User::getUserType, 2)
                    .eq(User::getStatus, 1));
            
            if (relatedUser != null) {
                Map<String, Object> contact = new HashMap<>();
                contact.put("id", relatedUser.getId());
                contact.put("name", relatedUser.getName());
                contact.put("phone", relatedUser.getPhone());
                contact.put("relation", "子女");
                contacts.add(contact);
            }
        }
        
        return Result.success(contacts);
    }

    /**
     * 获取绑定的老人列表（子女端使用）
     */
    @GetMapping("/bindElderly")
    public Result<List<User>> getBindElderly(@RequestAttribute Long userId) {
        User currentUser = userService.getById(userId);
        if (currentUser == null || currentUser.getFamilyId() == null) {
            // 如果没有家庭ID，返回空列表
            return Result.success(List.of());
        }
        
        // 查询同一家庭的老人
        List<User> elderlyList = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getFamilyId, currentUser.getFamilyId())
                .eq(User::getUserType, 1) // 老人
                .eq(User::getStatus, 1));
        
        return Result.success(elderlyList);
    }

    /**
     * 绑定老人（通过手机号）
     */
    @PostMapping("/bindElderly")
    public Result<Void> bindElderly(@RequestAttribute Long userId, @RequestBody Map<String, String> body) {
        String elderlyPhone = body.get("phone");
        if (elderlyPhone == null || elderlyPhone.isEmpty()) {
            throw new RuntimeException("请输入老人手机号");
        }
        
        // 查找老人
        User elderly = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, elderlyPhone)
                .eq(User::getUserType, 1));
        
        if (elderly == null) {
            throw new RuntimeException("未找到该老人账号");
        }
        
        User currentUser = userService.getById(userId);
        
        // 如果老人已有家庭，将子女加入该家庭
        if (elderly.getFamilyId() != null) {
            currentUser.setFamilyId(elderly.getFamilyId());
            userService.updateById(currentUser);
        } else if (currentUser.getFamilyId() != null) {
            // 如果子女已有家庭，将老人加入该家庭
            elderly.setFamilyId(currentUser.getFamilyId());
            userService.updateById(elderly);
        } else {
            // 都没有家庭，创建新家庭
            Family family = new Family();
            family.setAddress(elderly.getAddress());
            familyMapper.insert(family);
            
            elderly.setFamilyId(family.getId());
            currentUser.setFamilyId(family.getId());
            userService.updateById(elderly);
            userService.updateById(currentUser);
        }
        
        return Result.success();
    }

    /**
     * 解绑老人
     */
    @PostMapping("/unbindElderly")
    public Result<Void> unbindElderly(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        Object elderlyIdObj = body.get("elderlyId");
        if (elderlyIdObj == null) {
            throw new RuntimeException("请选择要解绑的老人");
        }
        Long elderlyId = Long.parseLong(elderlyIdObj.toString());
        
        User currentUser = userService.getById(userId);
        User elderly = userService.getById(elderlyId);
        
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (elderly == null) {
            throw new RuntimeException("老人不存在");
        }
        
        if (currentUser.getFamilyId() == null) {
            throw new RuntimeException("您还未绑定任何老人");
        }
        
        if (!currentUser.getFamilyId().equals(elderly.getFamilyId())) {
            throw new RuntimeException("该老人未与您绑定");
        }
        
        Long familyId = currentUser.getFamilyId();
        
        // 将老人从家庭中移除 - 使用 UpdateWrapper 强制更新 null 值
        userService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                .eq(User::getId, elderlyId)
                .set(User::getFamilyId, null));
        
        // 检查家庭中是否还有其他老人
        long elderlyCount = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getFamilyId, familyId)
                .eq(User::getUserType, 1));
        
        // 如果家庭中没有老人了，将子女也从家庭中移除
        if (elderlyCount == 0) {
            userService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                    .eq(User::getId, userId)
                    .set(User::getFamilyId, null));
        }
        
        return Result.success();
    }
    
    // ========== 管理后台接口 ==========
    
    /**
     * 管理员绑定紧急联系人（通过手机号或身份证号）
     */
    @PostMapping("/admin/bindContact")
    public Result<Void> adminBindContact(@RequestBody Map<String, Object> body) {
        Long elderlyId = body.get("elderlyId") != null ? 
                Long.parseLong(body.get("elderlyId").toString()) : null;
        String contactPhone = (String) body.get("contactPhone");
        String contactIdCard = (String) body.get("contactIdCard");
        
        if (elderlyId == null) {
            throw new RuntimeException("请选择老人");
        }
        
        if ((contactPhone == null || contactPhone.isEmpty()) && 
            (contactIdCard == null || contactIdCard.isEmpty())) {
            throw new RuntimeException("请输入联系人手机号或身份证号");
        }
        
        User elderly = userService.getById(elderlyId);
        if (elderly == null || elderly.getUserType() != 1) {
            throw new RuntimeException("老人不存在");
        }
        
        // 查找子女用户
        User contact = null;
        if (contactPhone != null && !contactPhone.isEmpty()) {
            contact = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, contactPhone)
                    .eq(User::getUserType, 2)
                    .eq(User::getStatus, 1));
        }
        if (contact == null && contactIdCard != null && !contactIdCard.isEmpty()) {
            contact = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getIdCard, contactIdCard)
                    .eq(User::getUserType, 2)
                    .eq(User::getStatus, 1));
        }
        
        if (contact == null) {
            throw new RuntimeException("未找到该子女账号，请确认手机号或身份证号正确");
        }
        
        // 建立家庭关系
        if (elderly.getFamilyId() != null) {
            // 老人已有家庭，将子女加入
            contact.setFamilyId(elderly.getFamilyId());
            userService.updateById(contact);
        } else if (contact.getFamilyId() != null) {
            // 子女已有家庭，将老人加入
            elderly.setFamilyId(contact.getFamilyId());
            userService.updateById(elderly);
        } else {
            // 都没有家庭，创建新家庭
            Family family = new Family();
            family.setAddress(elderly.getAddress());
            familyMapper.insert(family);
            
            elderly.setFamilyId(family.getId());
            contact.setFamilyId(family.getId());
            userService.updateById(elderly);
            userService.updateById(contact);
        }
        
        // 更新关联身份证号
        if (elderly.getRelatedIdCard() == null || elderly.getRelatedIdCard().isEmpty()) {
            elderly.setRelatedIdCard(contact.getIdCard());
            userService.updateById(elderly);
        }
        if (contact.getRelatedIdCard() == null || contact.getRelatedIdCard().isEmpty()) {
            contact.setRelatedIdCard(elderly.getIdCard());
            userService.updateById(contact);
        }
        
        return Result.success();
    }
    
    /**
     * 管理员解绑紧急联系人
     */
    @PostMapping("/admin/unbindContact")
    public Result<Void> adminUnbindContact(@RequestBody Map<String, Object> body) {
        Long elderlyId = body.get("elderlyId") != null ? 
                Long.parseLong(body.get("elderlyId").toString()) : null;
        Long contactId = body.get("contactId") != null ? 
                Long.parseLong(body.get("contactId").toString()) : null;
        
        if (elderlyId == null || contactId == null) {
            throw new RuntimeException("参数错误");
        }
        
        User elderly = userService.getById(elderlyId);
        User contact = userService.getById(contactId);
        
        if (elderly == null || contact == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (elderly.getFamilyId() == null || !elderly.getFamilyId().equals(contact.getFamilyId())) {
            throw new RuntimeException("该联系人未与老人绑定");
        }
        
        // 将子女从家庭中移除
        contact.setFamilyId(null);
        contact.setRelatedIdCard(null);
        userService.updateById(contact);
        
        return Result.success();
    }
    
    /**
     * 获取老人的紧急联系人列表（管理后台使用）
     */
    @GetMapping("/admin/contacts/{elderlyId}")
    public Result<List<User>> getElderlyContacts(@PathVariable Long elderlyId) {
        User elderly = userService.getById(elderlyId);
        if (elderly == null || elderly.getFamilyId() == null) {
            return Result.success(List.of());
        }
        
        List<User> contacts = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getFamilyId, elderly.getFamilyId())
                .eq(User::getUserType, 2)
                .eq(User::getStatus, 1));
        
        return Result.success(contacts);
    }
}
