package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderly.common.Result;
import com.elderly.common.annotation.Log;
import com.elderly.entity.Building;
import com.elderly.entity.BuildingWorker;
import com.elderly.entity.User;
import com.elderly.mapper.BuildingMapper;
import com.elderly.mapper.BuildingWorkerMapper;
import com.elderly.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@RestController
@RequestMapping("/api/admin/building")
@RequiredArgsConstructor
public class AdminBuildingController {

    private final BuildingMapper buildingMapper;
    private final BuildingWorkerMapper buildingWorkerMapper;
    private final UserMapper userMapper;
//    private final AdminLogService adminLogService;
//    private final UserService userService;
//    private final JwtUtil jwtUtil;

    // ==================== 楼栋管理接口 ====================

    /**
     * 分页查询楼栋列表
     */
    @GetMapping("/page")
    public Result<Page<Building>> getBuildingPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<Building> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<Building>()
                .like(name != null && !name.isEmpty(), Building::getName, name)
                .eq(status != null, Building::getStatus, status)
                .orderByAsc(Building::getCode)
                .orderByAsc(Building::getName);
        return Result.success(buildingMapper.selectPage(page, wrapper));
    }

    /**
     * 获取楼栋详情
     */
    @GetMapping("/{id}")
    public Result<Building> getBuildingById(@PathVariable Long id) {
        Building building = buildingMapper.selectById(id);
        if (building == null) {
            return Result.error("楼栋不存在");
        }
        return Result.success(building);
    }

    /**
     * 新增楼栋
     */
    @PostMapping
    @Log(module = "楼栋管理", operationType = 1, desc = "'新增楼栋:'+ #building.name")
    @CacheEvict(value = "building_names", allEntries = true)
    public Result<Void> addBuilding(@RequestBody Building building) {
        // 检查名称是否重复（包括逻辑删除的记录）
        Building existingDeleted = buildingMapper.selectOne(
            new LambdaQueryWrapper<Building>()
                .eq(Building::getName, building.getName())
        );
        if (existingDeleted != null) {
            // 更新逻辑删除的记录
            existingDeleted.setStatus(building.getStatus() != null ? building.getStatus() : 1);
            buildingMapper.updateById(existingDeleted);
            return Result.success();
        }
        
        // 检查名称是否重复（未逻辑删除的记录）
        int count = buildingMapper.countByNameExcludeId(building.getName(), 0L);
        if (count > 0) {
            return Result.error("楼栋名称已存在");
        }
        
        building.setStatus(building.getStatus() != null ? building.getStatus() : 1);
        buildingMapper.insert(building);
        return Result.success();
    }

    /**
     * 修改楼栋
     */
    @PutMapping("/{id}")
    @Log(module = "楼栋管理", operationType = 2, desc = "'修改楼栋信息: ' + #building.name")
    @CacheEvict(value = "building_names", allEntries = true)
    public Result<Void> updateBuilding(@PathVariable Long id, @RequestBody Building building) {
        Building existing = buildingMapper.selectById(id);
        if (existing == null) {
            return Result.error("楼栋不存在");
        }
        // 检查名称是否重复
        int count = buildingMapper.countByNameExcludeId(building.getName(), id);
        if (count > 0) {
            return Result.error("楼栋名称已存在");
        }
        building.setId(id);
        buildingMapper.updateById(building);
        return Result.success();
    }

    /**
     * 删除楼栋
     */
    @DeleteMapping("/{id}")
    @Log(module = "楼栋管理", operationType = 3, desc = "'删除楼栋ID: ' + #id")
    @CacheEvict(value = "building_names", allEntries = true)
    public Result<Void> deleteBuilding(@PathVariable Long id) {
        Building existing = buildingMapper.selectById(id);
        if (existing == null) {
            return Result.error("楼栋不存在");
        }
        // 检查是否有配送员关联
        long workerCount = buildingWorkerMapper.selectCount(
            new LambdaQueryWrapper<BuildingWorker>()
                .eq(BuildingWorker::getBuilding, existing.getName())
                .eq(BuildingWorker::getStatus, 1)
        );
        if (workerCount > 0) {
            return Result.error("该楼栋已分配配送员，请先移除配送员后再删除");
        }
        buildingMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 修改楼栋状态
     */
    @PutMapping("/{id}/status")
    @Log(module = "楼栋管理", operationType = 2, desc = "'修改楼栋状态 ID: ' + #id + ' 状态: ' + (#status==1?'启用':'禁用')")
    @CacheEvict(value = "building_names", allEntries = true)
    public Result<Void> updateBuildingStatus(@PathVariable Long id, @RequestParam Integer status) {
        Building existing = buildingMapper.selectById(id);
        if (existing == null) {
            return Result.error("楼栋不存在");
        }
        Building update = new Building();
        update.setId(id);
        update.setStatus(status);
        buildingMapper.updateById(update);
        String action = status == 1 ? "启用" : "禁用";
        return Result.success();
    }

    /**
     * 获取所有启用的楼栋名称（用于下拉选择）
     */
    @GetMapping("/all-names")
    @Cacheable(value = "building_names", key = "'list'")
    public Result<List<String>> getAllBuildingNames() {
        List<String> names = buildingMapper.selectAllBuildingNames();
        return Result.success(names);
    }

    // ==================== 楼栋配送员管理接口 ====================

    /**
     * 获取所有楼栋列表（带配送员信息）
     */
    @GetMapping("/list")
    @Cacheable(value = "building_names", key = "'list_with_workers'")
    public Result<List<Map<String, Object>>> getBuildingList() {
        // 获取所有启用的楼栋
        List<String> buildings = buildingMapper.selectAllBuildingNames();
        
        // 获取所有配送员
        List<User> allWorkers = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getUserType, 3)
                .eq(User::getWorkerType, 1)
                .eq(User::getStatus, 1)
        );
        Map<Long, User> workerMap = allWorkers.stream()
            .collect(Collectors.toMap(User::getId, u -> u));
        
        // 获取所有楼栋配送员关联
        List<BuildingWorker> allBuildingWorkers = buildingWorkerMapper.selectList(
            new LambdaQueryWrapper<BuildingWorker>().eq(BuildingWorker::getStatus, 1)
        );
        Map<String, List<BuildingWorker>> buildingWorkerMap = allBuildingWorkers.stream()
            .collect(Collectors.groupingBy(BuildingWorker::getBuilding));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (String building : buildings) {
            Map<String, Object> item = new HashMap<>();
            item.put("building", building);
            
            List<BuildingWorker> workers = buildingWorkerMap.getOrDefault(building, new ArrayList<>());
            List<Map<String, Object>> workerInfoList = workers.stream().map(bw -> {
                Map<String, Object> workerInfo = new HashMap<>();
                workerInfo.put("id", bw.getId());
                workerInfo.put("workerId", bw.getWorkerId());
                workerInfo.put("isPrimary", bw.getIsPrimary());
                User worker = workerMap.get(bw.getWorkerId());
                if (worker != null) {
                    workerInfo.put("workerName", worker.getName());
                    workerInfo.put("workerPhone", worker.getPhone());
                }
                return workerInfo;
            }).collect(Collectors.toList());
            
            item.put("workers", workerInfoList);
            item.put("workerCount", workerInfoList.size());
            result.add(item);
        }
        
        return Result.success(result);
    }

    /**
     * 获取所有可用配送员
     */
    @GetMapping("/workers")
    public Result<List<Map<String, Object>>> getAvailableWorkers() {
        List<User> workers = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getUserType, 3)
                .eq(User::getWorkerType, 1)
                .eq(User::getStatus, 1)
        );
        
        List<Map<String, Object>> result = workers.stream().map(w -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", w.getId());
            map.put("name", w.getName());
            map.put("phone", w.getPhone());
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }

    /**
     * 为楼栋分配配送员
     */
    @PostMapping("/assign")
    @Log(module = "楼栋管理", operationType = 2, desc = "'分配配送员, 楼栋: ' + #params['building'] + ' 员工ID: ' + #params['workerId']")
    public Result<Void> assignWorker(@RequestBody Map<String, Object> params) {
        String building = (String) params.get("building");
        Long workerId = Long.valueOf(params.get("workerId").toString());
        Integer isPrimary = params.get("isPrimary") != null ? 
            Integer.valueOf(params.get("isPrimary").toString()) : 0;
        
        // 检查该配送员是否已分配给其他楼栋
        BuildingWorker existingInOther = buildingWorkerMapper.selectOne(
            new LambdaQueryWrapper<BuildingWorker>()
                .ne(BuildingWorker::getBuilding, building)
                .eq(BuildingWorker::getWorkerId, workerId)
                .eq(BuildingWorker::getStatus, 1)
        );
        if (existingInOther != null) {
            return Result.error("该配送员已分配给" + existingInOther.getBuilding() + "，不能重复分配");
        }
        
        // 检查是否已存在于当前楼栋
        BuildingWorker existing = buildingWorkerMapper.selectOne(
            new LambdaQueryWrapper<BuildingWorker>()
                .eq(BuildingWorker::getBuilding, building)
                .eq(BuildingWorker::getWorkerId, workerId)
        );
        
        if (existing != null) {
            // 更新
            existing.setIsPrimary(isPrimary);
            existing.setStatus(1);
            buildingWorkerMapper.updateById(existing);
        } else {
            // 新增
            BuildingWorker bw = new BuildingWorker();
            bw.setBuilding(building);
            bw.setWorkerId(workerId);
            bw.setIsPrimary(isPrimary);
            bw.setStatus(1);
            buildingWorkerMapper.insert(bw);
        }
        
        // 如果设为主要负责人，取消该楼栋其他人的主要负责人标记
        if (isPrimary == 1) {
            buildingWorkerMapper.update(null, 
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<BuildingWorker>()
                    .eq(BuildingWorker::getBuilding, building)
                    .ne(BuildingWorker::getWorkerId, workerId)
                    .set(BuildingWorker::getIsPrimary, 0)
            );
        }
        
        User worker = userMapper.selectById(workerId);

        return Result.success();
    }

    /**
     * 移除楼栋配送员
     */
    @DeleteMapping("/remove")
    @Log(module = "楼栋管理", operationType = 3, desc = "'移除配送员, 楼栋: ' + #building + ' 员工ID: ' + #workerId")
    public Result<Void> removeWorker(@RequestParam String building, @RequestParam Long workerId) {
        buildingWorkerMapper.delete(
            new LambdaQueryWrapper<BuildingWorker>()
                .eq(BuildingWorker::getBuilding, building)
                .eq(BuildingWorker::getWorkerId, workerId)
        );
        User worker = userMapper.selectById(workerId);
        return Result.success();
    }

    /**
     * 设置主要负责人
     */
    @PostMapping("/set-primary")
    @Log(module = "楼栋管理", operationType = 2, desc = "'设置主要负责人, 楼栋: ' + #params['building'] + ' 员工ID: ' + #params['workerId']")
    public Result<Void> setPrimary(@RequestBody Map<String, Object> params) {
        String building = (String) params.get("building");
        Long workerId = Long.valueOf(params.get("workerId").toString());
        
        // 先取消该楼栋所有主要负责人
        buildingWorkerMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<BuildingWorker>()
                .eq(BuildingWorker::getBuilding, building)
                .set(BuildingWorker::getIsPrimary, 0)
        );
        
        // 设置新的主要负责人
        buildingWorkerMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<BuildingWorker>()
                .eq(BuildingWorker::getBuilding, building)
                .eq(BuildingWorker::getWorkerId, workerId)
                .set(BuildingWorker::getIsPrimary, 1)
        );
        
        return Result.success();
    }

    /**
     * 获取所有楼栋名称列表（用于下拉选择）
     */
    @GetMapping("/names")
    public Result<List<String>> getBuildingNames() {
        List<String> buildings = buildingMapper.selectAllBuildingNames();
        return Result.success(buildings);
    }

    /**
     * 根据楼栋获取该楼栋分配的配送员列表
     */
    @GetMapping("/workers-by-building")
    public Result<List<Map<String, Object>>> getWorkersByBuilding(@RequestParam String building) {
        // 获取该楼栋分配的配送员
        List<BuildingWorker> buildingWorkers = buildingWorkerMapper.selectList(
            new LambdaQueryWrapper<BuildingWorker>()
                .eq(BuildingWorker::getBuilding, building)
                .eq(BuildingWorker::getStatus, 1)
        );
        
        if (buildingWorkers.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        
        // 获取配送员详细信息
        List<Long> workerIds = buildingWorkers.stream()
            .map(BuildingWorker::getWorkerId)
            .collect(Collectors.toList());
        
        List<User> workers = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .in(User::getId, workerIds)
                .eq(User::getStatus, 1)
        );
        
        // 构建返回结果，包含是否为主要负责人信息
        Map<Long, BuildingWorker> bwMap = buildingWorkers.stream()
            .collect(Collectors.toMap(BuildingWorker::getWorkerId, bw -> bw));
        
        List<Map<String, Object>> result = workers.stream().map(w -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", w.getId());
            map.put("name", w.getName());
            map.put("phone", w.getPhone());
            BuildingWorker bw = bwMap.get(w.getId());
            map.put("isPrimary", bw != null ? bw.getIsPrimary() : 0);
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }
}
