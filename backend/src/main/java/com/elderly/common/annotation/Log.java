package com.elderly.common.annotation;

import java.lang.annotation.*;

/**
 * 自定义操作日志注解
 */
@Target(ElementType.METHOD) // 注解只能用在方法上
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时有效
@Documented
public @interface Log {
    /** 模块名称 (例如：楼栋管理) */
    String module() default "";

    /** 操作类型 (1-新增 2-修改 3-删除 4-查询 5-其他) */
    int operationType() default 5;

    /** 操作描述 (例如：新增楼栋) */
    String desc() default "";
}