package com.elderly.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLSyntaxErrorException;

/**
 * 全局异常处理器
 * 作用：拦截所有 Controller 抛出的异常，统一转换为标准 Result 格式返回给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常 (RuntimeException)
     * 例如：throw new RuntimeException("该用户不存在");
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        // 业务异常通常是预期的，warn 级别即可，不需要打印堆栈
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 处理数据库唯一键冲突异常 (Duplicate entry)
     * 例如：注册时手机号重复
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据库唯一键冲突: {}", e.getMessage());
        return Result.error("操作失败：数据已存在，请检查关键字段（如手机号、身份证）是否重复");
    }

    /**
     * 处理数据库完整性约束异常 (Data truncation 等)
     * 例如：字段长度不够，或者必填字段为空
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("数据库完整性异常: ", e);
        return Result.error("操作失败：数据格式不正确或超出长度限制");
    }

    /**
     * 处理 SQL 语法错误 (通常是开发阶段的问题)
     */
    @ExceptionHandler(SQLSyntaxErrorException.class)
    public Result<Void> handleSQLSyntaxErrorException(SQLSyntaxErrorException e) {
        log.error("SQL语法错误: ", e);
        return Result.error("系统内部错误：数据库操作异常");
    }

    /**
     * 处理请求参数类型不匹配
     * 例如：接口要 Long 类型 id，前端传了 "abc"
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: 参数名={}, 值={}", e.getName(), e.getValue());
        return Result.error("参数类型错误，请检查输入格式");
    }

    /**
     * 处理缺少必填参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少必填参数: {}", e.getParameterName());
        return Result.error("缺少必填参数: " + e.getParameterName());
    }

    /**
     * 处理请求方法不支持
     * 例如：接口定义了 @PostMapping，前端用了 GET 请求
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMethod());
        return Result.error("请求方法不支持: " + e.getMethod());
    }

    /**
     * 处理 404 接口不存在 (需要配置 spring.mvc.throw-exception-if-no-handler-found=true)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return Result.error(404, "接口不存在");
    }

    /**
     * 兜底处理：所有未捕获的异常
     * 这里必须打印完整堆栈，方便排查 BUG
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统未知异常: ", e); // 打印完整堆栈到控制台/日志文件
        return Result.error("系统繁忙，请稍后重试");
    }
}