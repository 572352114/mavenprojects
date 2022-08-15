package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

//@Mapper  告诉sprigng框架此接口的实现类由Mybatis负责创建，并将其实现类对象存储到spring容器中
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
