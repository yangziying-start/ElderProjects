package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.PointsTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PointsTransactionMapper extends BaseMapper<PointsTransaction> {
    
    @Select("SELECT COALESCE(SUM(amount), 0) FROM points_transaction WHERE type = #{type}")
    Long sumAmountByType(@Param("type") Integer type);
}
