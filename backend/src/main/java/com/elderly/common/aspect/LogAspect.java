package com.elderly.common.aspect;

import com.alibaba.fastjson2.JSON;
import com.elderly.common.annotation.Log;
import com.elderly.entity.User;
import com.elderly.service.AdminLogService;
import com.elderly.service.UserService;
import com.elderly.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final AdminLogService adminLogService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // SpEL 解析器，用于解析 #building.name 这种语法
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint point, Log logAnnotation) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = null;
        String errorMsg = null;
        Integer status = 1;

        try {
            result = point.proceed();
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            saveLog(point, logAnnotation, result, status, errorMsg);
        }
        return result;
    }

    private void saveLog(ProceedingJoinPoint point, Log logAnnotation, Object result, Integer status, String errorMsg) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
            if (request == null) return;

            // 1. 获取用户信息
            String token = request.getHeader("Authorization");
            Long userId = null;
            String userName = "未知/系统";
            if (token != null && !token.isEmpty()) {
                try {
                    userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
                    User user = userService.getById(userId);
                    if (user != null) userName = user.getName();
                } catch (Exception ignored) {}
            }

            // 2. 解析动态描述 (核心升级点)
            String description = getDynamicDescription(point, logAnnotation.desc());

            // 3. 获取其他参数
            String params = argsArrayToString(point.getArgs());
            String responseStr = (result != null) ? JSON.toJSONString(result) : null;
            if (responseStr != null && responseStr.length() > 2000) responseStr = responseStr.substring(0, 2000) + "..."; // 截断防止数据库报错

            MethodSignature signature = (MethodSignature) point.getSignature();
            String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
            String requestUrl = request.getRequestURI();
            String ip = request.getRemoteAddr();

            // 4. 入库
            adminLogService.log(
                    userId, userName, logAnnotation.module(), logAnnotation.operationType(),
                    description, methodName, requestUrl, params,
                    responseStr, ip, status, errorMsg
            );

        } catch (Exception e) {
            log.error("日志记录异常", e);
        }
    }

    /**
     * 解析 SpEL 表达式，将 #building.name 替换为真实值
     */
    private String getDynamicDescription(ProceedingJoinPoint point, String descTemplate) {
        if (!descTemplate.contains("#")) {
            return descTemplate; // 如果没有#，直接返回原描述
        }
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Object[] args = point.getArgs();
            String[] paramNames = parameterNameDiscoverer.getParameterNames(method);

            if (paramNames == null || paramNames.length == 0) return descTemplate;

            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }

            Expression expression = parser.parseExpression(descTemplate);
            // 使用 String.class 强制转换为字符串，如果模板是 "'删除:' + #id"，这里就会拼接
            return expression.getValue(context, String.class);
        } catch (Exception e) {
            log.warn("解析日志描述失败: {}", descTemplate);
            return descTemplate; // 解析失败降级为原描述
        }
    }

    private String argsArrayToString(Object[] paramsArray) {
        try {
            if (paramsArray == null || paramsArray.length == 0) return "";
            List<Object> logArgs = Arrays.stream(paramsArray)
                    .filter(arg -> (!(arg instanceof HttpServletRequest) &&
                            !(arg instanceof HttpServletResponse) &&
                            !(arg instanceof MultipartFile)))
                    .collect(Collectors.toList());
            return JSON.toJSONString(logArgs);
        } catch (Exception e) {
            return "参数解析异常";
        }
    }
}