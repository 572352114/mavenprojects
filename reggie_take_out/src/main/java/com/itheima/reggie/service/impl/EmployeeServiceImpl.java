package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

//@Service :
//      context:component-scan base-package="com.study.persistent"
// 在applicationContext.xml配置⽂件中加上这⼀⾏以后，将⾃动扫描指定路径下的包

// 如果⼀个类带了@Service注解，将⾃动注册到Spring容器，不需要再在applicationContext.xml配置⽂件中定义bean了，
// 类似的还包括@Component、@Repository、@Controller。
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService {
}
