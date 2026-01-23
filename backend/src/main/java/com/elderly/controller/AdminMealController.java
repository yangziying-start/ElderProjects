package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.entity.*;
import com.elderly.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台 - 社区餐饮管理
 */
@RestController
@RequestMapping("/api/admin/meal")
@RequiredArgsConstructor
public class AdminMealController {

    private final MealWeeklyMenuMapper weeklyMenuMapper;
    private final MealDailyDishMapper dailyDishMapper;
    private final MealDeliveryConfigMapper deliveryConfigMapper;
    private final MealOrderMapper mealOrderMapper;
    private final MealDishTemplateMapper dishTemplateMapper;
    private final MealWorkerScheduleMapper workerScheduleMapper;
    private final UserMapper userMapper;

    // ========== 配送员列表（用于分配配送任务） ==========

    @GetMapping("/delivery-workers")
    public Result<List<User>> listDeliveryWorkers() {
        // 获取用户类型为服务人员且服务人员类型为配送员的用户 (userType=3, workerType=1)
        return Result.success(userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUserType, 3)
                        .eq(User::getWorkerType, 1)  // 只返回配送员
                        .eq(User::getStatus, 1)
                        .orderByDesc(User::getCreateTime)));
    }

    // ========== 周食谱管理 ==========

    @GetMapping("/weekly-menus")
    public Result<Page<MealWeeklyMenu>> pageWeeklyMenus(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<MealWeeklyMenu> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MealWeeklyMenu> wrapper = new LambdaQueryWrapper<MealWeeklyMenu>()
                .orderByDesc(MealWeeklyMenu::getWeekStartDate);
        return Result.success(weeklyMenuMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/weekly-menus/{id}")
    public Result<MealWeeklyMenu> getWeeklyMenu(@PathVariable Long id) {
        return Result.success(weeklyMenuMapper.selectById(id));
    }

    @PostMapping("/weekly-menus")
    public Result<MealWeeklyMenu> addWeeklyMenu(@RequestBody MealWeeklyMenu menu) {
        // 如果没有指定日期，自动计算下一周的起止日期
        if (menu.getWeekStartDate() == null) {
            LocalDate today = LocalDate.now();
            LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            menu.setWeekStartDate(nextMonday);
            menu.setWeekEndDate(nextMonday.plusDays(6));
        }
        if (menu.getStatus() == null) {
            menu.setStatus(0); // 默认草稿状态
        }
        weeklyMenuMapper.insert(menu);
        return Result.success(menu);
    }

    @PutMapping("/weekly-menus/{id}")
    public Result<Void> updateWeeklyMenu(@PathVariable Long id, @RequestBody MealWeeklyMenu menu) {
        menu.setId(id);
        weeklyMenuMapper.updateById(menu);
        return Result.success();
    }

    @PostMapping("/weekly-menus/{id}/publish")
    public Result<Void> publishWeeklyMenu(@PathVariable Long id) {
        MealWeeklyMenu menu = new MealWeeklyMenu();
        menu.setId(id);
        menu.setStatus(1);
        menu.setPublishTime(LocalDateTime.now());
        weeklyMenuMapper.updateById(menu);
        return Result.success();
    }

    @DeleteMapping("/weekly-menus/{id}")
    public Result<Void> deleteWeeklyMenu(@PathVariable Long id) {
        // 检查是否有关联订单
        MealWeeklyMenu menu = weeklyMenuMapper.selectById(id);
        if (menu != null && menu.getStatus() == 1) {
            // 检查该周是否有订单
            long orderCount = mealOrderMapper.selectCount(
                    new LambdaQueryWrapper<MealOrder>()
                            .ge(MealOrder::getDishDate, menu.getWeekStartDate())
                            .le(MealOrder::getDishDate, menu.getWeekEndDate()));
            if (orderCount > 0) {
                return Result.error("该周食谱已有关联订单，无法删除");
            }
        }
        // 删除关联的每日菜品
        dailyDishMapper.delete(new LambdaQueryWrapper<MealDailyDish>()
                .eq(MealDailyDish::getWeeklyMenuId, id));
        weeklyMenuMapper.deleteById(id);
        return Result.success();
    }

    // ========== 每日菜品管理 ==========

    @GetMapping("/weekly-menus/{menuId}/dishes")
    public Result<List<MealDailyDish>> getDishes(@PathVariable Long menuId) {
        List<MealDailyDish> dishes = dailyDishMapper.selectList(
                new LambdaQueryWrapper<MealDailyDish>()
                        .eq(MealDailyDish::getWeeklyMenuId, menuId)
                        .orderByAsc(MealDailyDish::getDishDate)
                        .orderByAsc(MealDailyDish::getMealType)
                        .orderByAsc(MealDailyDish::getSort));
        return Result.success(dishes);
    }

    @GetMapping("/weekly-menus/{menuId}/dishes-grouped")
    public Result<Map<String, List<MealDailyDish>>> getDishesGrouped(@PathVariable Long menuId) {
        List<MealDailyDish> dishes = dailyDishMapper.selectList(
                new LambdaQueryWrapper<MealDailyDish>()
                        .eq(MealDailyDish::getWeeklyMenuId, menuId)
                        .orderByAsc(MealDailyDish::getSort));
        // 按日期和餐次分组
        Map<String, List<MealDailyDish>> grouped = dishes.stream()
                .collect(Collectors.groupingBy(d -> d.getDishDate() + "_" + d.getMealType()));
        return Result.success(grouped);
    }

    @PostMapping("/dishes")
    public Result<MealDailyDish> addDish(@RequestBody MealDailyDish dish) {
        if (dish.getStatus() == null) {
            dish.setStatus(1);
        }
        if (dish.getSort() == null) {
            dish.setSort(0);
        }
        dailyDishMapper.insert(dish);
        return Result.success(dish);
    }

    @PutMapping("/dishes/{id}")
    public Result<Void> updateDish(@PathVariable Long id, @RequestBody MealDailyDish dish) {
        dish.setId(id);
        dailyDishMapper.updateById(dish);
        return Result.success();
    }

    @DeleteMapping("/dishes/{id}")
    public Result<Void> deleteDish(@PathVariable Long id) {
        dailyDishMapper.deleteById(id);
        return Result.success();
    }

    @PostMapping("/dishes/{id}/status")
    public Result<Void> updateDishStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        MealDailyDish dish = new MealDailyDish();
        dish.setId(id);
        dish.setStatus(body.get("status"));
        dailyDishMapper.updateById(dish);
        return Result.success();
    }

    // ========== 配送时间配置 ==========

    @GetMapping("/delivery-configs")
    public Result<List<MealDeliveryConfig>> getDeliveryConfigs() {
        return Result.success(deliveryConfigMapper.selectList(
                new LambdaQueryWrapper<MealDeliveryConfig>()
                        .orderByAsc(MealDeliveryConfig::getMealType)));
    }

    @PutMapping("/delivery-configs/{id}")
    public Result<Void> updateDeliveryConfig(@PathVariable Long id, @RequestBody MealDeliveryConfig config) {
        config.setId(id);
        deliveryConfigMapper.updateById(config);
        return Result.success();
    }

    // ========== 餐饮订单管理 ==========

    @GetMapping("/orders")
    public Result<Page<MealOrder>> pageOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String dishDate,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer mealType) {
        Page<MealOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MealOrder> wrapper = new LambdaQueryWrapper<MealOrder>()
                .eq(dishDate != null, MealOrder::getDishDate, dishDate)
                .eq(status != null, MealOrder::getStatus, status)
                .eq(mealType != null, MealOrder::getMealType, mealType)
                .orderByDesc(MealOrder::getCreateTime);
        Page<MealOrder> result = mealOrderMapper.selectPage(pageParam, wrapper);
        // 填充菜品信息
        result.getRecords().forEach(order -> {
            if (order.getDishId() != null) {
                order.setDish(dailyDishMapper.selectById(order.getDishId()));
            }
        });
        return Result.success(result);
    }

    @GetMapping("/orders/{id}")
    public Result<MealOrder> getOrder(@PathVariable Long id) {
        MealOrder order = mealOrderMapper.selectById(id);
        if (order != null && order.getDishId() != null) {
            order.setDish(dailyDishMapper.selectById(order.getDishId()));
        }
        return Result.success(order);
    }

    @PostMapping("/orders/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        MealOrder order = new MealOrder();
        order.setId(id);
        order.setStatus(body.get("status"));
        mealOrderMapper.updateById(order);
        return Result.success();
    }

    @GetMapping("/orders/stats")
    public Result<Map<String, Object>> getOrderStats() {
        LocalDate today = LocalDate.now();
        // 今日订单数
        long todayCount = mealOrderMapper.selectCount(
                new LambdaQueryWrapper<MealOrder>().eq(MealOrder::getDishDate, today));
        // 待配送数
        long pendingCount = mealOrderMapper.selectCount(
                new LambdaQueryWrapper<MealOrder>()
                        .eq(MealOrder::getDishDate, today)
                        .eq(MealOrder::getStatus, 0));
        // 配送中数
        long deliveringCount = mealOrderMapper.selectCount(
                new LambdaQueryWrapper<MealOrder>()
                        .eq(MealOrder::getDishDate, today)
                        .eq(MealOrder::getStatus, 1));
        return Result.success(Map.of(
                "todayCount", todayCount,
                "pendingCount", pendingCount,
                "deliveringCount", deliveringCount));
    }

    // ========== 菜单库管理 ==========

    @GetMapping("/dish-templates")
    public Result<Page<MealDishTemplate>> pageDishTemplates(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer mealType,
            @RequestParam(required = false) String keyword) {
        Page<MealDishTemplate> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MealDishTemplate> wrapper = new LambdaQueryWrapper<MealDishTemplate>()
                .eq(category != null, MealDishTemplate::getCategory, category)
                .like(mealType != null, MealDishTemplate::getMealTypes, String.valueOf(mealType))
                .like(keyword != null && !keyword.isEmpty(), MealDishTemplate::getDishName, keyword)
                .eq(MealDishTemplate::getStatus, 1)
                .orderByDesc(MealDishTemplate::getCreateTime);
        return Result.success(dishTemplateMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/dish-templates/all")
    public Result<List<MealDishTemplate>> listAllDishTemplates(
            @RequestParam(required = false) Integer mealType) {
        LambdaQueryWrapper<MealDishTemplate> wrapper = new LambdaQueryWrapper<MealDishTemplate>()
                .like(mealType != null, MealDishTemplate::getMealTypes, String.valueOf(mealType))
                .eq(MealDishTemplate::getStatus, 1)
                .orderByAsc(MealDishTemplate::getCategory)
                .orderByAsc(MealDishTemplate::getDishName);
        return Result.success(dishTemplateMapper.selectList(wrapper));
    }

    @PostMapping("/dish-templates")
    public Result<MealDishTemplate> addDishTemplate(@RequestBody MealDishTemplate template) {
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        dishTemplateMapper.insert(template);
        return Result.success(template);
    }

    @PutMapping("/dish-templates/{id}")
    public Result<Void> updateDishTemplate(@PathVariable Long id, @RequestBody MealDishTemplate template) {
        template.setId(id);
        dishTemplateMapper.updateById(template);
        return Result.success();
    }

    @DeleteMapping("/dish-templates/{id}")
    public Result<Void> deleteDishTemplate(@PathVariable Long id) {
        dishTemplateMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 从菜单库选择菜品添加到周食谱
     */
    @PostMapping("/weekly-menus/{menuId}/add-from-template")
    public Result<MealDailyDish> addDishFromTemplate(
            @PathVariable Long menuId,
            @RequestBody Map<String, Object> body) {
        Long templateId = Long.valueOf(body.get("templateId").toString());
        String dishDate = body.get("dishDate").toString();
        Integer mealType = Integer.valueOf(body.get("mealType").toString());
        Integer price = body.get("price") != null ? Integer.valueOf(body.get("price").toString()) : null;
        
        MealDishTemplate template = dishTemplateMapper.selectById(templateId);
        if (template == null) {
            return Result.error("菜品模板不存在");
        }
        
        MealDailyDish dish = new MealDailyDish();
        dish.setWeeklyMenuId(menuId);
        dish.setDishDate(LocalDate.parse(dishDate));
        dish.setMealType(mealType);
        dish.setDishName(template.getDishName());
        dish.setPrice(price != null ? price : template.getPrice());
        dish.setImage(template.getImage());
        dish.setDescription(template.getDescription());
        dish.setNutritionInfo(template.getNutritionInfo());
        dish.setStatus(1);
        dish.setSort(0);
        
        dailyDishMapper.insert(dish);
        return Result.success(dish);
    }

    // ========== 配送员值班管理 ==========

    /**
     * 获取值班安排列表
     */
    @GetMapping("/worker-schedules")
    public Result<List<MealWorkerSchedule>> getWorkerSchedules(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<MealWorkerSchedule> schedules = workerScheduleMapper.selectByDateRange(startDate, endDate);
        // 填充配送员信息
        for (MealWorkerSchedule schedule : schedules) {
            if (schedule.getWorkerId() != null) {
                User worker = userMapper.selectById(schedule.getWorkerId());
                if (worker != null) {
                    schedule.setWorkerName(worker.getName());
                    schedule.setWorkerPhone(worker.getPhone());
                }
            }
        }
        return Result.success(schedules);
    }

    /**
     * 新增值班安排
     */
    @PostMapping("/worker-schedules")
    public Result<MealWorkerSchedule> addWorkerSchedule(@RequestBody MealWorkerSchedule schedule) {
        // 检查是否已存在相同日期、餐次、配送员的值班
        LambdaQueryWrapper<MealWorkerSchedule> wrapper = new LambdaQueryWrapper<MealWorkerSchedule>()
                .eq(MealWorkerSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(MealWorkerSchedule::getWorkerId, schedule.getWorkerId())
                .eq(schedule.getMealType() != null, MealWorkerSchedule::getMealType, schedule.getMealType())
                .eq(MealWorkerSchedule::getDeleted, 0);
        if (workerScheduleMapper.selectCount(wrapper) > 0) {
            return Result.error("该配送员在此日期已有值班安排");
        }
        
        if (schedule.getStatus() == null) {
            schedule.setStatus(1);
        }
        schedule.setDeleted(0);
        workerScheduleMapper.insert(schedule);
        return Result.success(schedule);
    }

    /**
     * 批量新增值班安排（按日期范围）
     */
    @PostMapping("/worker-schedules/batch")
    public Result<Void> batchAddWorkerSchedule(@RequestBody Map<String, Object> body) {
        Long workerId = Long.valueOf(body.get("workerId").toString());
        LocalDate startDate = LocalDate.parse(body.get("startDate").toString());
        LocalDate endDate = LocalDate.parse(body.get("endDate").toString());
        Integer mealType = body.get("mealType") != null ? Integer.valueOf(body.get("mealType").toString()) : null;
        String buildings = body.get("buildings") != null ? body.get("buildings").toString() : null;
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            // 检查是否已存在
            LambdaQueryWrapper<MealWorkerSchedule> wrapper = new LambdaQueryWrapper<MealWorkerSchedule>()
                    .eq(MealWorkerSchedule::getScheduleDate, current)
                    .eq(MealWorkerSchedule::getWorkerId, workerId)
                    .eq(mealType != null, MealWorkerSchedule::getMealType, mealType)
                    .eq(MealWorkerSchedule::getDeleted, 0);
            if (workerScheduleMapper.selectCount(wrapper) == 0) {
                MealWorkerSchedule schedule = new MealWorkerSchedule();
                schedule.setWorkerId(workerId);
                schedule.setScheduleDate(current);
                schedule.setMealType(mealType);
                schedule.setBuildings(buildings);
                schedule.setStatus(1);
                schedule.setDeleted(0);
                workerScheduleMapper.insert(schedule);
            }
            current = current.plusDays(1);
        }
        return Result.success();
    }

    /**
     * 修改值班安排
     */
    @PutMapping("/worker-schedules/{id}")
    public Result<Void> updateWorkerSchedule(@PathVariable Long id, @RequestBody MealWorkerSchedule schedule) {
        schedule.setId(id);
        workerScheduleMapper.updateById(schedule);
        return Result.success();
    }

    /**
     * 删除值班安排
     */
    @DeleteMapping("/worker-schedules/{id}")
    public Result<Void> deleteWorkerSchedule(@PathVariable Long id) {
        workerScheduleMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 获取指定日期的值班配送员
     */
    @GetMapping("/worker-schedules/by-date")
    public Result<List<Map<String, Object>>> getSchedulesByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) Integer mealType) {
        List<MealWorkerSchedule> schedules;
        if (mealType != null) {
            schedules = workerScheduleMapper.selectByDateAndMealType(date, mealType);
        } else {
            schedules = workerScheduleMapper.selectList(
                    new LambdaQueryWrapper<MealWorkerSchedule>()
                            .eq(MealWorkerSchedule::getScheduleDate, date)
                            .eq(MealWorkerSchedule::getStatus, 1)
                            .eq(MealWorkerSchedule::getDeleted, 0));
        }
        
        List<Map<String, Object>> result = schedules.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("workerId", s.getWorkerId());
            map.put("mealType", s.getMealType());
            map.put("buildings", s.getBuildings());
            User worker = userMapper.selectById(s.getWorkerId());
            if (worker != null) {
                map.put("workerName", worker.getName());
                map.put("workerPhone", worker.getPhone());
            }
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }
}
