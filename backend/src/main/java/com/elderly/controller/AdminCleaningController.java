package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.common.annotation.Log;
import com.elderly.entity.*;
import com.elderly.mapper.*;
import com.elderly.service.AdminLogService;
import com.elderly.service.UserService;
import com.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 居家保洁管理
 */
@RestController
@RequestMapping("/api/admin/cleaning")
@RequiredArgsConstructor
public class AdminCleaningController {

    private final CleaningServiceMapper cleaningServiceMapper;
    private final CleaningWorkerScheduleMapper workerScheduleMapper;
    private final CleaningOrderMapper cleaningOrderMapper;
    private final UserMapper userMapper;
    private final AdminLogService adminLogService;
    private final UserService userService;
    private final JwtUtil jwtUtil;


    // ========== 保洁服务项目管理 ==========

    @GetMapping("/services")
    public Result<List<CleaningService>> listServices() {
        return Result.success(cleaningServiceMapper.selectList(
                new LambdaQueryWrapper<CleaningService>()
                        .orderByAsc(CleaningService::getSort)));
    }

    @GetMapping("/services/{id}")
    public Result<CleaningService> getService(@PathVariable Long id) {
        return Result.success(cleaningServiceMapper.selectById(id));
    }

    @PostMapping("/services")
    @Log(module = "保洁管理", operationType = 1, desc = "'新增保洁服务: ' + #service.name")
    public Result<CleaningService> addService(@RequestBody CleaningService service) {
        if (service.getStatus() == null) {
            service.setStatus(1);
        }
        if (service.getSort() == null) {
            service.setSort(0);
        }
        cleaningServiceMapper.insert(service);
        return Result.success(service);
    }

    @PutMapping("/services/{id}")
    @Log(module = "保洁管理", operationType = 2, desc = "'修改保洁服务: ' + #service.name")
    public Result<Void> updateService(@PathVariable Long id, @RequestBody CleaningService service) {
        service.setId(id);
        cleaningServiceMapper.updateById(service);
        return Result.success();
    }

    @PostMapping("/services/{id}/status")
    @Log(module = "保洁管理", operationType = 2, desc = "'修改保洁服务状态 ID: ' + #id + ' 状态: ' + (#body['status']==1?'启用':'禁用')")
    public Result<Void> updateServiceStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        CleaningService existing = cleaningServiceMapper.selectById(id);
        CleaningService service = new CleaningService();
        service.setId(id);
        service.setStatus(body.get("status"));
        cleaningServiceMapper.updateById(service);
        return Result.success();
    }

    @DeleteMapping("/services/{id}")
    @Log(module = "保洁管理", operationType = 3, desc = "'删除保洁服务 ID: ' + #id")
    public Result<Void> deleteService(@PathVariable Long id) {
        // 检查是否有关联订单
        long orderCount = cleaningOrderMapper.selectCount(
                new LambdaQueryWrapper<CleaningOrder>().eq(CleaningOrder::getServiceId, id));
        if (orderCount > 0) {
            return Result.error("该服务项目有关联订单，无法删除");
        }
        CleaningService service = cleaningServiceMapper.selectById(id);
        cleaningServiceMapper.deleteById(id);
        return Result.success();
    }

    // ========== 保洁员管理 ==========

    @GetMapping("/workers")
    public Result<List<User>> listWorkers() {
        // 获取用户类型为服务人员且服务人员类型为保洁员的用户 (userType=3, workerType=2)
        return Result.success(userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUserType, 3)
                        .eq(User::getWorkerType, 2)  // 只返回保洁员
                        .eq(User::getStatus, 1)
                        .orderByDesc(User::getCreateTime)));
    }

    // ========== 保洁员排班管理 ==========

    @GetMapping("/schedules")
    public Result<List<CleaningWorkerSchedule>> listSchedules(
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<CleaningWorkerSchedule> wrapper = new LambdaQueryWrapper<CleaningWorkerSchedule>()
                .eq(workerId != null, CleaningWorkerSchedule::getWorkerId, workerId)
                .ge(startDate != null, CleaningWorkerSchedule::getScheduleDate, startDate)
                .le(endDate != null, CleaningWorkerSchedule::getScheduleDate, endDate)
                .orderByAsc(CleaningWorkerSchedule::getScheduleDate)
                .orderByAsc(CleaningWorkerSchedule::getStartTime);
        List<CleaningWorkerSchedule> schedules = workerScheduleMapper.selectList(wrapper);
        // 填充保洁员信息
        schedules.forEach(s -> {
            if (s.getWorkerId() != null) {
                s.setWorker(userMapper.selectById(s.getWorkerId()));
            }
        });
        return Result.success(schedules);
    }

    @GetMapping("/schedules/{id}")
    public Result<CleaningWorkerSchedule> getSchedule(@PathVariable Long id) {
        CleaningWorkerSchedule schedule = workerScheduleMapper.selectById(id);
        if (schedule != null && schedule.getWorkerId() != null) {
            schedule.setWorker(userMapper.selectById(schedule.getWorkerId()));
        }
        return Result.success(schedule);
    }

    @PostMapping("/schedules")
    @Log(module = "保洁管理", operationType = 1, desc = "'新增保洁排班, 员工ID: ' + #schedule.workerId + ' 日期: ' + #schedule.scheduleDate")
    public Result<CleaningWorkerSchedule> addSchedule(@RequestBody CleaningWorkerSchedule schedule) {
        if (schedule.getStatus() == null) {
            schedule.setStatus(1);
        }
        // 检查同一保洁员同一天同一时间段是否已有排班
        String conflict = checkScheduleConflict(schedule, null);
        if (conflict != null) {
            return Result.error(conflict);
        }
        workerScheduleMapper.insert(schedule);
        return Result.success(schedule);
    }

    @PostMapping("/schedules/batch")
    @Log(module = "保洁管理", operationType = 1, desc = "'批量新增保洁排班 ' + #schedules.size() + ' 条'")
    public Result<Void> batchAddSchedules(@RequestBody List<CleaningWorkerSchedule> schedules) {
        for (CleaningWorkerSchedule schedule : schedules) {
            if (schedule.getStatus() == null) {
                schedule.setStatus(1);
            }
            // 检查同一保洁员同一天同一时间段是否已有排班
            String conflict = checkScheduleConflict(schedule, null);
            if (conflict != null) {
                return Result.error(conflict);
            }
            workerScheduleMapper.insert(schedule);
        }
        return Result.success();
    }

    @PutMapping("/schedules/{id}")
    @Log(module = "保洁管理", operationType = 2, desc = "'修改保洁排班 ID: ' + #id")
    public Result<Void> updateSchedule(@PathVariable Long id, @RequestBody CleaningWorkerSchedule schedule) {
        schedule.setId(id);
        // 检查同一保洁员同一天同一时间段是否已有排班（排除当前记录）
        String conflict = checkScheduleConflict(schedule, id);
        if (conflict != null) {
            return Result.error(conflict);
        }
        workerScheduleMapper.updateById(schedule);
        return Result.success();
    }
    
    /**
     * 检查排班时间段是否冲突
     */
    private String checkScheduleConflict(CleaningWorkerSchedule schedule, Long excludeId) {
        if (schedule.getWorkerId() == null || schedule.getScheduleDate() == null 
            || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return null;
        }
        
        // 查询同一保洁员同一天的排班
        LambdaQueryWrapper<CleaningWorkerSchedule> wrapper = new LambdaQueryWrapper<CleaningWorkerSchedule>()
                .eq(CleaningWorkerSchedule::getWorkerId, schedule.getWorkerId())
                .eq(CleaningWorkerSchedule::getScheduleDate, schedule.getScheduleDate());
        if (excludeId != null) {
            wrapper.ne(CleaningWorkerSchedule::getId, excludeId);
        }
        
        List<CleaningWorkerSchedule> existingSchedules = workerScheduleMapper.selectList(wrapper);
        
        for (CleaningWorkerSchedule existing : existingSchedules) {
            // 检查时间段是否重叠
            if (isTimeOverlap(schedule.getStartTime(), schedule.getEndTime(), 
                             existing.getStartTime(), existing.getEndTime())) {
                return "该保洁员在 " + schedule.getScheduleDate() + " " + 
                       existing.getStartTime() + "-" + existing.getEndTime() + " 已有排班";
            }
        }
        return null;
    }
    
    /**
     * 检查两个时间段是否重叠
     */
    private boolean isTimeOverlap(java.time.LocalTime start1, java.time.LocalTime end1,
                                   java.time.LocalTime start2, java.time.LocalTime end2) {
        return !(end1.compareTo(start2) <= 0 || start1.compareTo(end2) >= 0);
    }

    @DeleteMapping("/schedules/{id}")
    @Log(module = "保洁管理", operationType = 3, desc = "'删除保洁排班 ID: ' + #id")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        CleaningWorkerSchedule schedule = workerScheduleMapper.selectById(id);
        if (schedule != null) {
            // 检查该时段是否有预约
            long orderCount = cleaningOrderMapper.selectCount(
                    new LambdaQueryWrapper<CleaningOrder>()
                            .eq(CleaningOrder::getWorkerId, schedule.getWorkerId())
                            .eq(CleaningOrder::getServiceDate, schedule.getScheduleDate())
                            .ne(CleaningOrder::getStatus, 4)); // 排除已取消的
            if (orderCount > 0) {
                return Result.error("该排班时段有预约订单，无法删除");
            }
        }
        workerScheduleMapper.deleteById(id);
        return Result.success();
    }

    // ========== 保洁订单管理 ==========

    @GetMapping("/orders")
    public Result<Page<CleaningOrder>> pageOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String serviceDate,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) Integer status) {
        Page<CleaningOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CleaningOrder> wrapper = new LambdaQueryWrapper<CleaningOrder>()
                .eq(serviceDate != null, CleaningOrder::getServiceDate, serviceDate)
                .eq(workerId != null, CleaningOrder::getWorkerId, workerId)
                .eq(status != null, CleaningOrder::getStatus, status)
                .orderByDesc(CleaningOrder::getCreateTime);
        Page<CleaningOrder> result = cleaningOrderMapper.selectPage(pageParam, wrapper);
        // 填充关联信息
        result.getRecords().forEach(order -> {
            if (order.getServiceId() != null) {
                order.setService(cleaningServiceMapper.selectById(order.getServiceId()));
            }
            if (order.getWorkerId() != null) {
                order.setWorker(userMapper.selectById(order.getWorkerId()));
            }
        });
        return Result.success(result);
    }

    @GetMapping("/orders/{id}")
    public Result<CleaningOrder> getOrder(@PathVariable Long id) {
        CleaningOrder order = cleaningOrderMapper.selectById(id);
        if (order != null) {
            if (order.getServiceId() != null) {
                order.setService(cleaningServiceMapper.selectById(order.getServiceId()));
            }
            if (order.getWorkerId() != null) {
                order.setWorker(userMapper.selectById(order.getWorkerId()));
            }
        }
        return Result.success(order);
    }

    @PostMapping("/orders/{id}/status")
    @Log(module = "保洁管理", operationType = 2, desc = "'修改保洁订单状态 ID: ' + #id + ' 新状态: ' + #body['status']")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        CleaningOrder order = new CleaningOrder();
        order.setId(id);
        order.setStatus(body.get("status"));
        cleaningOrderMapper.updateById(order);
        return Result.success();
    }

    @GetMapping("/orders/stats")
    public Result<Map<String, Object>> getOrderStats() {
        LocalDate today = LocalDate.now();
        // 今日订单数
        long todayCount = cleaningOrderMapper.selectCount(
                new LambdaQueryWrapper<CleaningOrder>().eq(CleaningOrder::getServiceDate, today));
        // 待服务数
        long pendingCount = cleaningOrderMapper.selectCount(
                new LambdaQueryWrapper<CleaningOrder>()
                        .eq(CleaningOrder::getServiceDate, today)
                        .eq(CleaningOrder::getStatus, 0));
        // 服务中数
        long inProgressCount = cleaningOrderMapper.selectCount(
                new LambdaQueryWrapper<CleaningOrder>()
                        .eq(CleaningOrder::getServiceDate, today)
                        .eq(CleaningOrder::getStatus, 1));
        return Result.success(Map.of(
                "todayCount", todayCount,
                "pendingCount", pendingCount,
                "inProgressCount", inProgressCount));
    }
}
