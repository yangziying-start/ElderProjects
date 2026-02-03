package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.MedicalCircuitBreaker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MedicalCircuitBreakerMapper extends BaseMapper<MedicalCircuitBreaker> {
    
    @Select("SELECT * FROM medical_circuit_breaker WHERE doctor_id = #{doctorId} LIMIT 1")
    MedicalCircuitBreaker selectByDoctorId(@Param("doctorId") Long doctorId);
}
