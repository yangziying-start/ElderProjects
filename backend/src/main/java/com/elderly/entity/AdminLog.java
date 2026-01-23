package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin_log")
public class AdminLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 操作管理员ID */
    private Long adminId;
    
    /** 操作管理员姓名 */
    private String adminName;
    
    /** 操作模块 */
    private String module;
    
    /** 操作类型: 1-新增 2-修改 3-删除 4-查询 5-其他 */
    private Integer operationType;
    
    /** 操作描述 */
    private String description;
    
    /** 请求方法 */
    private String method;
    
    /** 请求URL */
    private String requestUrl;
    
    /** 请求参数 */
    private String requestParams;
    
    /** 响应结果 */
    private String responseResult;
    
    /** IP地址 */
    private String ip;
    
    /** 操作状态: 0-失败 1-成功 */
    private Integer status;
    
    /** 错误信息 */
    private String errorMsg;
    
    /** 操作时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
