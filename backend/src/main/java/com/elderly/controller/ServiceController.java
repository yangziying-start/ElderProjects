package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.common.Result;
import com.elderly.dto.TimeSlotVO;
import com.elderly.entity.Building;
import com.elderly.entity.ServiceCategory;
import com.elderly.entity.ServiceItem;
import com.elderly.mapper.BuildingMapper;
import com.elderly.mapper.ServiceCategoryMapper;
import com.elderly.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceItemService serviceItemService;
    private final ServiceCategoryMapper categoryMapper;
    private final BuildingMapper buildingMapper;

    @GetMapping("/categories")
    public Result<List<ServiceCategory>> getCategories() {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<ServiceCategory>()
                        .eq(ServiceCategory::getStatus, 1)
                        .orderByAsc(ServiceCategory::getSort)));
    }

    @GetMapping("/items")
    public Result<List<ServiceItem>> getItems(@RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStatus, 1)
                .eq(categoryId != null, ServiceItem::getCategoryId, categoryId);
        return Result.success(serviceItemService.list(wrapper));
    }

    @GetMapping("/items/{id}")
    public Result<ServiceItem> getItemDetail(@PathVariable Long id) {
        return Result.success(serviceItemService.getById(id));
    }

    @GetMapping("/items/{id}/capacity")
    public Result<Integer> getCapacity(@PathVariable Long id,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(serviceItemService.getAvailableCapacity(id, date));
    }
    
    /**
     * 获取服务项目在指定日期的可预约时间段
     */
    @GetMapping("/items/{id}/time-slots")
    public Result<List<TimeSlotVO>> getTimeSlots(@PathVariable Long id,
                                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(serviceItemService.getAvailableTimeSlots(id, date));
    }
    
    /**
     * 检查服务分类是否为医疗类型
     */
    @GetMapping("/categories/{id}/is-medical")
    public Result<Boolean> isMedicalCategory(@PathVariable Long id) {
        ServiceCategory category = categoryMapper.selectById(id);
        return Result.success(category != null && category.getIsMedical() != null && category.getIsMedical() == 1);
    }
    
    /**
     * 获取所有启用的楼栋列表（用于地址选择）
     */
    @GetMapping("/buildings")
    public Result<List<Map<String, Object>>> getBuildingList() {
        List<Building> buildings = buildingMapper.selectList(
                new LambdaQueryWrapper<Building>()
                        .eq(Building::getStatus, 1)
                        .orderByAsc(Building::getCode)
                        .orderByAsc(Building::getName));
        
        List<Map<String, Object>> result = buildings.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("building", b.getName());
            map.put("code", b.getCode());
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }
}
