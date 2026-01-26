package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("building_worker")
public class BuildingWorker {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 楼栋名称 */
    private String building;
    
    /** 配送员ID */
    private Long workerId;
    
    /** 是否主要负责人: 0-否 1-是 */
    private Integer isPrimary;
    
    /** 状态: 0-停用 1-启用 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /** 配送员信息（非数据库字段） */
    @TableField(exist = false)
    private User worker;
}
