package com.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderly.entity.User;

public interface UserService extends IService<User> {
    User getByOpenid(String openid);
    User getByPhone(String phone);
    User getByPhoneAndType(String phone, Integer userType);
    
    /**
     * 手机号密码登录（不同端账号隔离）
     */
    String loginByPhone(String phone, String password, Integer userType);
    
    /**
     * 注册新用户
     */
    String register(String phone, String password, Integer userType, String name);
    
    /**
     * 注册新用户（含身份证号）
     */
    String register(String phone, String password, Integer userType, String name, String idCard);
    
    /**
     * 注册新用户（含身份证号和服务人员类型）
     */
    String register(String phone, String password, Integer userType, String name, String idCard, Integer workerType);
    
    /**
     * 注册新用户（含关联身份证号，用于管理员注册老人/子女并自动建立关联）
     */
    String registerWithRelation(String phone, String password, Integer userType, String name, String idCard, Integer workerType, String relatedIdCard, String address);
    
    /**
     * 管理员登录
     */
    String adminLogin(String username, String password);
    
    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置密码（管理员操作）
     */
    void resetPassword(Long userId, String newPassword);
}
