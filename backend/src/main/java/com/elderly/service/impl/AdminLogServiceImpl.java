package com.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderly.entity.AdminLog;
import com.elderly.mapper.AdminLogMapper;
import com.elderly.service.AdminLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminLogServiceImpl extends ServiceImpl<AdminLogMapper, AdminLog> implements AdminLogService {

    @Override
    @Async
    public void log(Long adminId, String adminName, String module, Integer operationType,
                    String description, String method, String requestUrl, String requestParams,
                    String responseResult, String ip, Integer status, String errorMsg) {
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setModule(module);
        log.setOperationType(operationType);
        log.setDescription(description);
        log.setMethod(method);
        log.setRequestUrl(requestUrl);
        log.setRequestParams(requestParams != null && requestParams.length() > 2000 ? requestParams.substring(0, 2000) : requestParams);
        log.setResponseResult(responseResult != null && responseResult.length() > 2000 ? responseResult.substring(0, 2000) : responseResult);
        log.setIp(ip);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        save(log);
    }

    @Override
    @Async
    public void log(Long adminId, String adminName, String module, String description) {
        log(adminId, adminName, module, 5, description, null, null, null, null, null, 1, null);
    }

    @Override
    public Page<AdminLog> pageQuery(Integer page, Integer size, Long adminId, String module, Integer operationType) {
        LambdaQueryWrapper<AdminLog> wrapper = new LambdaQueryWrapper<AdminLog>()
                .eq(adminId != null, AdminLog::getAdminId, adminId)
                .eq(module != null && !module.isEmpty(), AdminLog::getModule, module)
                .eq(operationType != null, AdminLog::getOperationType, operationType)
                .orderByDesc(AdminLog::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }
}
