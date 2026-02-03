package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.MedicalDoctorDuty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MedicalDoctorDutyMapper extends BaseMapper<MedicalDoctorDuty> {
    
    @Select("SELECT * FROM medical_doctor_duty WHERE duty_date = #{date} AND status = 1 ORDER BY duty_type, start_time")
    List<MedicalDoctorDuty> selectByDate(@Param("date") LocalDate date);
    
    @Select("SELECT * FROM medical_doctor_duty WHERE doctor_id = #{doctorId} AND duty_date = #{date} AND status = 1 LIMIT 1")
    MedicalDoctorDuty selectByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
}
