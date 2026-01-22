package com.elderly.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private String orderType; // cleaning, meal, medical
    private String serviceName;
    private String elderlyName;
    private String elderlyPhone;
    private String workerName;
    private String workerPhone;
    private String appointmentTime;
    private String appointmentDate;
    private String address;
    private BigDecimal amount;
    private Integer status;
    private String statusText;
    private String remark;
    private String serviceCode;
    private String evidence;
    private java.time.LocalDateTime createTime;
    
    /** 服务项目ID */
    private Long serviceItemId;
    /** 老人ID */
    private Long elderlyId;
}
