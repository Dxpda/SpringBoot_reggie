package com.example.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeDao extends BaseMapper<Employee> {

    @Select("select * from reggie.employee where id = #{id} ")
    Employee All(@Param("id") Integer id);
}
