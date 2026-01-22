package com.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderly.common.Result;
import com.elderly.entity.BuildingWorker;
import com.elderly.entity.User;
import com.elderly.mapper.BuildingWorkerMapper;
import com.elderly.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/building")
@RequiredArgsConstructor
public class AdminBuildingController {

    private final BuildingWorkerMapper buildingWorkerMapper;
    private final UserMapper userMapper;

    /**
     * 获取所有楼栋列表（带配送员信息）
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getBuildingList() {
        // 获取所有楼栋
        List<String> buildings = buildingWorkerMapper.selectAllBuildings();
        
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
    public Result<Void> assignWorker(@RequestBody Map<String, Object> params) {
        String building = (String) params.get("building");
        Long workerId = Long.valueOf(params.get("workerId").toString());
        Integer isPrimary = params.get("isPrimary") != null ? 
            Integer.valueOf(params.get("isPrimary").toString()) : 0;
        
        // 检查是否已存在
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
        
        return Result.success();
    }

    /**
     * 移除楼栋配送员
     */
    @DeleteMapping("/remove")
    public Result<Void> removeWorker(@RequestParam String building, @RequestParam Long workerId) {
        buildingWorkerMapper.delete(
            new LambdaQueryWrapper<BuildingWorker>()
                .eq(BuildingWorker::getBuilding, building)
                .eq(BuildingWorker::getWorkerId, workerId)
        );
        return Result.success();
    }

    /**
     * 设置主要负责人
     */
    @PostMapping("/set-primary")
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
}
