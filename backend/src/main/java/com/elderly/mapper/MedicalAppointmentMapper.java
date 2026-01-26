package com.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderly.entity.MedicalAppointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;

@Mapper
public interface MedicalAppointmentMapper extends BaseMapper<MedicalAppointment> {
    
    @Select("SELECT COUNT(*) FROM medical_appointment WHERE doctor_id = #{doctorId} AND appointment_date = #{date} AND status IN (0, 1) AND deleted = 0")
    int countDoctorQueue(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    @Select("SELECT MAX(queue_number) FROM medical_appointment WHERE doctor_id = #{doctorId} AND appointment_date = #{date} AND deleted = 0")
    Integer selectMaxQueueNumber(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
}
