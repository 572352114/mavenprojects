package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

//import org.springframework.util.StringUtils;
import org.apache.commons.lang.StringUtils;


import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 员工控制层
 */
@Slf4j
@RestController
//@RestController 作用：
//      该注解用于将Controller的方法返回的对象，通过适当的HttpMessageConverter转换为指定格式后，写入到Response对象的body数据区。
/*
1) 如果只是使用@RestController注解Controller，则Controller中的方法无法返回jsp页面，或者html，配置的视图解析器 InternalResourceViewResolver不起作用，返回的内容就是return 里的内容。
2) 如果需要返回到指定页面，则需要用 @Controller配合视图解析器InternalResourceViewResolver才行。
3) 如果需要返回JSON，XML或自定义mediaType内容到页面，则需要在对应的方法上加上@ResponseBody注解。
*/
@RequestMapping("/employee")
/*
* 在Spring MVC 中使用 @RequestMapping 来映射请求，也就是通过它来指定控制器可以处理哪些URL请求
* */
public class EmployeeController {
    /*
@Autowired可以对成员变量、方法和构造函数进行标注，来完成自动装配的工作，这里必须明确：@Autowired是根据类型进行自动装配的，如果需要按名称进行装配，则需要配合@Qualifier使用；
@Autowired标注可以放在成员变量上，也可以放在成员变量的set方法上。前者，Spring会直接将UserDao类型的唯一一个bean赋值给userDao这个成员变量；后者，Spring会调用setUserDao方法来将UserDao类型的唯一一个bean装配到userDao这个属性。
Spring 2.5 引入了 @Autowired 注释，它可以对类成员变量、方法及构造函数进行标注，完成自动装配的工作。 通过 @Autowired的使用来消除 set ，get方法。*/
    @Autowired
    private EmployeeService employeeService;

    /*** 登录*/
    @PostMapping("/login")
    //因为 login.thml 登录界面返回的是json格式  所以这里需要加上@RequestBody注解
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){


        //4.用户名存在 emp就不为空  可以直接与emp.getpassword作比较 判断密码是否正确
        String password = employee.getPassword();
        //调用DigestUtils.md5DigestAsHex方法  加密密码
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //1.根据login.thml页面提供的用户名 查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //2.queryWrapper.eq  等值查询  实体类中的getUsername 和传进来的getUsername做等值查询    这是一个查询条件
        queryWrapper.eq(Employee::getUsername,employee.getUsername());

        //3. 需要调用 employeeService.getOne 方法   把查询条件放进去  然后去查询数据库中
        Employee emp = employeeService.getOne(queryWrapper);

        if(emp == null){
            return R.error("用户名不存在！");
        }

        if( !password.equals(emp.getPassword())){
            return R.error("密码错误！");
        }

        //5.判断用户是否被禁用
        if( emp.getStatus()== 0){
            return R.error("账号已被禁用！");
        }

        //6.登录成功，将员工id存入Session并返回登录成功结果
        /*  setAttribute(name,value): name：要设置的属性名 value:要设置的属性值
        *   1.把指定的属性设置为指定的值。如果不存在具有指定名称的属性，该方法将创建一个新属性。
            2.类似于getAttribute()方法，setAttribute()方法只能通过元素节点对象调用的函数。
        * */
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /*** 退出登录*/
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session 中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /***新增员工信息*/
    @PostMapping
    public R<String> addEmployee(HttpServletRequest request, @RequestBody Employee employee){
        //设置默认密码123456  先将密码进行md5加密
        String password = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(password);

/*      通过common文件夹下的  MyMetaObjectHandler 类统一处理 自动填充
        //创建时间以及更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获取当前用户id，创建用户人以及更新用户人
        Long empid= (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(empid);
        employee.setCreateUser(empid);
        */

        employeeService.save(employee);

        log.info("新增员工，员工信息：{}",employee.toString());
        return R.success("新增员工成功");
    }

    /***分页查询*/
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pagesize = {},yourname = {}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo =new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();

        //StringUtils.isNotEmpty 建议导入org.apache.commons.lang.StringUtils 这个包
        //而不是org.springframework.util.StringUtils
        /*
        * isNotBlank: 空值"",null和空格" "都为空。
        * isNotEmpty: 空值"",null为空,空格" "不为空。
        * */
        queryWrapper.like(StringUtils.isNotBlank(name),Employee::getName,name);

        //添加排序条件
        //queryWrapper.orderByDesc(Employee::getId);
        //queryWrapper.orderBy(true,true,Employee::getId);
        queryWrapper.orderByAsc(Employee::getId,Employee::getStatus);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /***更新*/
    @PutMapping
    public R<String> updateEmployee(@RequestBody Employee employee){
        //不需要获取Session中的id值 所以(HttpServletRequest request)可以去掉
    //public R<String> updateEmployee(HttpServletRequest request, @RequestBody Employee employee){
        log.info("员工信息：{}",employee.toString());

        //val empid = (Long) request.getSession().getAttribute("employee");

   /*   通过common文件夹下的  MyMetaObjectHandler 类统一处理 自动填充
        Long empid= (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(empid);
        employee.setUpdateTime(LocalDateTime.now());
        */

        //通过id更新数据
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /***获取员工id,查询员工信息*/
    //http://localhost:8080/employee/1    这种类型的用这个接收
    @GetMapping("/{id}")
    /*@PathVariable: 表明这个是路径变量* */
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息！，id为{}",id);

/*
        LambdaQueryWrapper<Employee> queryWrapper =new LambdaQueryWrapper();
        queryWrapper.eq(Employee::getId,id);
        Employee employee=employeeService.getById(queryWrapper);

        employeeService提供了getById方法
        */

        Employee employee=employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("查询失败");
    }
}
