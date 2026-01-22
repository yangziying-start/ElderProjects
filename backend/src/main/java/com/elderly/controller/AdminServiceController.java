package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.entity.ServiceCategory;
import com.elderly.entity.ServiceItem;
import com.elderly.entity.ServiceTimeSlot;
import com.elderly.mapper.ServiceCategoryMapper;
import com.elderly.mapper.ServiceTimeSlotMapper;
import com.elderly.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 服务管理
 */
@RestController
@RequestMapping("/api/admin/service")
@RequiredArgsConstructor
public class AdminServiceController {

    private final ServiceItemService serviceItemService;
    private final ServiceCategoryMapper categoryMapper;
    private final ServiceTimeSlotMapper timeSlotMapper;

    // ========== 服务分类管理 ==========

    @GetMapping("/categories")
    public Result<List<ServiceCategory>> listCategories() {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<ServiceCategory>()
                        .orderByAsc(ServiceCategory::getSort)));
    }

    @PostMapping("/categories")
    public Result<Void> addCategory(@RequestBody ServiceCategory category) {
        categoryMapper.insert(category);
        return Result.success();
    }

    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody ServiceCategory category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        // 检查是否有服务项目使用该分类
        long count = serviceItemService.count(new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getCategoryId, id));
        if (count > 0) {
            throw new RuntimeException("该分类下有服务项目，无法删除");
        }
        categoryMapper.deleteById(id);
        return Result.success();
    }

    // ========== 服务项目管理 ==========

    @GetMapping("/items")
    public Result<Page<ServiceItem>> pageItems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name) {
        
        Page<ServiceItem> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<ServiceItem>()
                .eq(categoryId != null, ServiceItem::getCategoryId, categoryId)
                .like(name != null, ServiceItem::getName, name)
                .orderByAsc(ServiceItem::getCategoryId)
                .orderByDesc(ServiceItem::getCreateTime);
        
        return Result.success(serviceItemService.page(pageParam, wrapper));
    }

    @GetMapping("/items/{id}")
    public Result<ServiceItem> getItem(@PathVariable Long id) {
        return Result.success(serviceItemService.getById(id));
    }

    @PostMapping("/items")
    public Result<Void> addItem(@RequestBody ServiceItem item) {
        serviceItemService.save(item);
        return Result.success();
    }

    @PutMapping("/items/{id}")
    public Result<Void> updateItem(@PathVariable Long id, @RequestBody ServiceItem item) {
        item.setId(id);
        serviceItemService.updateById(item);
        return Result.success();
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        serviceItemService.removeById(id);
        return Result.success();
    }

    @PostMapping("/items/{id}/status")
    public Result<Void> updateItemStatus(@PathVariable Long id, @RequestBody ServiceItem item) {
        ServiceItem update = new ServiceItem();
        update.setId(id);
        update.setStatus(item.getStatus());
        serviceItemService.updateById(update);
        return Result.success();
    }

    // ========== 时间段管理 ==========

    @GetMapping("/items/{itemId}/time-slots")
    public Result<List<ServiceTimeSlot>> getTimeSlots(@PathVariable Long itemId) {
        return Result.success(timeSlotMapper.selectList(
                new LambdaQueryWrapper<ServiceTimeSlot>()
                        .eq(ServiceTimeSlot::getServiceItemId, itemId)
                        .orderByAsc(ServiceTimeSlot::getStartTime)));
    }

    @PostMapping("/items/{itemId}/time-slots")
    public Result<Void> addTimeSlot(@PathVariable Long itemId, @RequestBody ServiceTimeSlot slot) {
        slot.setServiceItemId(itemId);
        timeSlotMapper.insert(slot);
        return Result.success();
    }

    @PutMapping("/time-slots/{id}")
    public Result<Void> updateTimeSlot(@PathVariable Long id, @RequestBody ServiceTimeSlot slot) {
        slot.setId(id);
        timeSlotMapper.updateById(slot);
        return Result.success();
    }

    @DeleteMapping("/time-slots/{id}")
    public Result<Void> deleteTimeSlot(@PathVariable Long id) {
        timeSlotMapper.deleteById(id);
        return Result.success();
    }
}
