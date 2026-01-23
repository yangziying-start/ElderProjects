package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.Building;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BuildingMapper extends BaseMapper<Building> {
    
    /**
     * 获取所有启用的楼栋名称列表
     */
    @Select("SELECT name FROM building WHERE status = 1 AND deleted = 0 ORDER BY code, name")
    List<String> selectAllBuildingNames();
    
    /**
     * 检查楼栋名称是否存在
     */
    @Select("SELECT COUNT(*) FROM building WHERE name = #{name} AND deleted = 0 AND id != #{excludeId}")
    int countByNameExcludeId(String name, Long excludeId);
}
