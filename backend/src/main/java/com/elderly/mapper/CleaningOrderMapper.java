package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.CleaningOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CleaningOrderMapper extends BaseMapper<CleaningOrder> {
    
    @Select("SELECT * FROM cleaning_order WHERE worker_id = #{workerId} AND service_date = #{date} AND status IN (0, 1) AND deleted = 0 ORDER BY start_time")
    List<CleaningOrder> selectWorkerBookedSlots(@Param("workerId") Long workerId, @Param("date") LocalDate date);
}
