package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.entity.*;
import com.elderly.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
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
}
