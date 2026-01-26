package com.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("building")
public class Building {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 楼栋名称 */
    private String name;
    
    /** 楼栋编号 */
    private String code;
    
    /** 楼层数 */
    private Integer floors;
    
    /** 单元数 */
    private Integer units;
    
    /** 备注 */
    private String remark;
    
    /** 状态: 0-停用 1-启用 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
