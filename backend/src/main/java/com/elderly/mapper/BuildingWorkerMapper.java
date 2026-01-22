package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.BuildingWorker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BuildingWorkerMapper extends BaseMapper<BuildingWorker> {
    
    /**
     * 根据楼栋获取配送员列表
     */
    @Select("SELECT * FROM building_worker WHERE building = #{building} AND status = 1 ORDER BY is_primary DESC")
    List<BuildingWorker> selectByBuilding(@Param("building") String building);
    
    /**
     * 获取楼栋的主要配送员
     */
    @Select("SELECT * FROM building_worker WHERE building = #{building} AND status = 1 AND is_primary = 1 LIMIT 1")
    BuildingWorker selectPrimaryByBuilding(@Param("building") String building);
    
    /**
     * 获取所有不重复的楼栋列表
     */
    @Select("SELECT DISTINCT building FROM family WHERE deleted = 0 ORDER BY building")
    List<String> selectAllBuildings();
}
