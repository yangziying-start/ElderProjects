package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.CleaningWorkerSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CleaningWorkerScheduleMapper extends BaseMapper<CleaningWorkerSchedule> {
    
    @Select("SELECT * FROM cleaning_worker_schedule WHERE schedule_date = #{date} AND status = 1 ORDER BY start_time")
    List<CleaningWorkerSchedule> selectByDate(@Param("date") LocalDate date);
    
    @Select("SELECT * FROM cleaning_worker_schedule WHERE worker_id = #{workerId} AND schedule_date = #{date} AND status = 1 LIMIT 1")
    CleaningWorkerSchedule selectByWorkerAndDate(@Param("workerId") Long workerId, @Param("date") LocalDate date);
}
