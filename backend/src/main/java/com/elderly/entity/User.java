package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String openid;
    private String phone;
    
    /** 密码(MD5加密) */
    @JsonIgnore
    private String password;
    
    private String name;
    
    /** 身份证号 */
    private String idCard;
    
    private String avatar;
    
    /** 用户类型: 1-老人 2-子女 3-服务人员 4-管理员 */
    private Integer userType;
    
    /** 服务人员类型: 1-配送员 2-保洁员 3-医疗人员 (仅userType=3时有效) */
    private Integer workerType;
    
    private String address;
    private Long familyId;
    
    /** 关联的身份证号（老人关联子女/子女关联老人） */
    private String relatedIdCard;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
