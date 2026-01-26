package com.elderly.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elderly.entity.AdminLog;

public interface AdminLogService extends IService<AdminLog> {
    
    /**
     * 记录操作日志
     */
    void log(Long adminId, String adminName, String module, Integer operationType, 
             String description, String method, String requestUrl, String requestParams,
             String responseResult, String ip, Integer status, String errorMsg);
    
    /**
     * 简化的日志记录
     */
    void log(Long adminId, String adminName, String module, String description);
    
    /**
     * 分页查询日志
     */
    Page<AdminLog> pageQuery(Integer page, Integer size, Long adminId, String module, Integer operationType);
}
