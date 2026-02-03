package com.elderly.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserImportDTO {
    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("关联身份证号") // 新增这一列，用于绑定家庭关系
    private String relatedIdCard;

    @ExcelProperty("用户类型") // 填写：老人、子女、服务人员
    private String userTypeStr;

    @ExcelProperty("地址")
    private String address;
    
    // 服务人员类型：配送员、保洁员、医疗人员（仅服务人员必填）
    @ExcelProperty("服务人员类型") 
    private String workerTypeStr;
}