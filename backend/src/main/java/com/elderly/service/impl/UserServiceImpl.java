package com.elderly.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderly.entity.Family;
import com.elderly.entity.User;
import com.elderly.mapper.FamilyMapper;
import com.elderly.mapper.UserMapper;
import com.elderly.service.UserService;
import com.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final FamilyMapper familyMapper;

    @Override
    public User getByOpenid(String openid) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
    }
    
    @Override
    public User getByPhone(String phone) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone), false);
    }
    
    @Override
    public User getByPhoneAndType(String phone, Integer userType) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getUserType, userType));
    }

    @Override
    public String loginByPhone(String phone, String password, Integer userType) {
        // 根据手机号和用户类型查找用户（不同端账号隔离）
        User user = getByPhoneAndType(phone, userType);
        
        if (user == null) {
            throw new RuntimeException("账号不存在，请先注册");
        }
        
        // 验证密码
        String encryptedPwd = DigestUtil.md5Hex(password);
        if (!encryptedPwd.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        return jwtUtil.generateToken(user.getId(), user.getUserType());
    }
    
    @Override
    public String register(String phone, String password, Integer userType, String name) {
        return register(phone, password, userType, name, null);
    }
    
    @Override
    public String register(String phone, String password, Integer userType, String name, String idCard) {
        return register(phone, password, userType, name, idCard, null);
    }
    
    @Override
    public String register(String phone, String password, Integer userType, String name, String idCard, Integer workerType) {
        // 检查该手机号在该端是否已注册
        User existUser = getByPhoneAndType(phone, userType);
        if (existUser != null) {
            throw new RuntimeException("该手机号已注册");
        }
        
        // 如果有身份证号，检查同类型用户身份证号是否已存在
        if (idCard != null && !idCard.isEmpty()) {
            User existIdCardUser = getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getIdCard, idCard)
                    .eq(User::getUserType, userType));
            if (existIdCardUser != null) {
                throw new RuntimeException("该身份证号已注册");
            }
        }
        
        // 如果没有传密码，使用身份证后6位作为初始密码
        String finalPassword = password;
        if ((finalPassword == null || finalPassword.isEmpty()) && idCard != null && idCard.length() >= 6) {
            finalPassword = idCard.substring(idCard.length() - 6);
        }
        if (finalPassword == null || finalPassword.isEmpty()) {
            finalPassword = "123456"; // 兜底默认密码
        }
        
        // 创建新用户
        User user = new User();
        user.setPhone(phone);
        user.setPassword(DigestUtil.md5Hex(finalPassword));
        user.setUserType(userType);
        user.setName(name != null ? name : generateDefaultName(phone, userType));
        user.setIdCard(idCard);
        user.setStatus(1);
        // 如果是服务人员，设置服务人员类型
        if (userType == 3 && workerType != null) {
            user.setWorkerType(workerType);
        }
        save(user);
        
        return jwtUtil.generateToken(user.getId(), user.getUserType());
    }
    
    @Override
    @Transactional
    public String registerWithRelation(String phone, String password, Integer userType, String name, 
            String idCard, Integer workerType, String relatedIdCard, String address) {
        // 检查该手机号在该端是否已注册
        User existUser = getByPhoneAndType(phone, userType);
        if (existUser != null) {
            throw new RuntimeException("该手机号已注册");
        }
        
        // 如果有身份证号，检查同类型用户身份证号是否已存在
        if (idCard != null && !idCard.isEmpty()) {
            User existIdCardUser = getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getIdCard, idCard)
                    .eq(User::getUserType, userType));
            if (existIdCardUser != null) {
                throw new RuntimeException("该身份证号已注册");
            }
        }
        
        // 如果没有传密码，使用身份证后6位作为初始密码
        String finalPassword = password;
        if ((finalPassword == null || finalPassword.isEmpty()) && idCard != null && idCard.length() >= 6) {
            finalPassword = idCard.substring(idCard.length() - 6);
        }
        if (finalPassword == null || finalPassword.isEmpty()) {
            finalPassword = "123456";
        }
        
        // 创建新用户
        User user = new User();
        user.setPhone(phone);
        user.setPassword(DigestUtil.md5Hex(finalPassword));
        user.setUserType(userType);
        user.setName(name != null ? name : generateDefaultName(phone, userType));
        user.setIdCard(idCard);
        user.setRelatedIdCard(relatedIdCard);
        user.setAddress(address);
        user.setStatus(1);
        
        if (userType == 3 && workerType != null) {
            user.setWorkerType(workerType);
        }
        
        // 处理家庭关联
        if (relatedIdCard != null && !relatedIdCard.isEmpty()) {
            // 根据关联身份证号查找对应用户
            // 如果当前是老人(1)，查找子女(2)；如果当前是子女(2)，查找老人(1)
            Integer relatedUserType = (userType == 1) ? 2 : 1;
            User relatedUser = getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getIdCard, relatedIdCard)
                    .eq(User::getUserType, relatedUserType));
            
            if (relatedUser != null) {
                // 找到关联用户，建立家庭关系
                if (relatedUser.getFamilyId() != null) {
                    // 关联用户已有家庭，加入该家庭
                    user.setFamilyId(relatedUser.getFamilyId());
                } else {
                    // 关联用户没有家庭，创建新家庭
                    Family family = new Family();
                    family.setAddress(address != null ? address : relatedUser.getAddress());
                    familyMapper.insert(family);
                    
                    user.setFamilyId(family.getId());
                    relatedUser.setFamilyId(family.getId());
                    // 双向关联
                    if (relatedUser.getRelatedIdCard() == null || relatedUser.getRelatedIdCard().isEmpty()) {
                        relatedUser.setRelatedIdCard(idCard);
                    }
                    updateById(relatedUser);
                }
            }
            // 如果没找到关联用户，先保存relatedIdCard，等关联用户注册时再建立关系
        }
        
        save(user);
        return jwtUtil.generateToken(user.getId(), user.getUserType());
    }

    @Override
    public String adminLogin(String username, String password) {
        // 管理员用用户名登录
        User admin = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getName, username)
                .eq(User::getUserType, 4));
        
        if (admin == null) {
            // 如果是默认管理员账号且不存在，自动创建
            if ("admin".equals(username) && "admin123".equals(password)) {
                admin = new User();
                admin.setName("admin");
                admin.setPassword(DigestUtil.md5Hex("admin123"));
                admin.setUserType(4);
                admin.setAdminRole(1); // 超级管理员
                admin.setStatus(1);
                save(admin);
                return jwtUtil.generateToken(admin.getId(), admin.getUserType());
            }
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查账号状态
        if (admin.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 验证密码
        String encryptedPwd = DigestUtil.md5Hex(password);
        if (!encryptedPwd.equals(admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        return jwtUtil.generateToken(admin.getId(), admin.getUserType());
    }
    
    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码
        if (!DigestUtil.md5Hex(oldPassword).equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 更新密码
        user.setPassword(DigestUtil.md5Hex(newPassword));
        updateById(user);
    }
    
    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(DigestUtil.md5Hex(newPassword));
        updateById(user);
    }
    
    /**
     * 生成默认用户名
     */
    private String generateDefaultName(String phone, Integer userType) {
        String suffix = phone.substring(phone.length() - 4);
        switch (userType) {
            case 1: return "老人" + suffix;
            case 2: return "家属" + suffix;
            case 3: return "服务员" + suffix;
            default: return "用户" + suffix;
        }
    }
}
