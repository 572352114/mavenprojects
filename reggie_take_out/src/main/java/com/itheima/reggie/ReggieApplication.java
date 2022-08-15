package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
/** @SpringBootApplication: 告诉Spring 这是一个Springboot应用*/
@SpringBootApplication
/** @ServletComponentScan:
 SpringBootApplication 上使用@ServletComponentScan 注解后
 Servlet可以直接通过@WebServlet注解自动注册
 Filter可以直接通过@WebFilter注解自动注册
 Listener可以直接通过@WebListener 注解自动注册*/
@ServletComponentScan

@EnableTransactionManagement
//Spring Boot使用事务非常简单，首先使用注解 @EnableTransactionManagement 开启事务支持后，然后在访问数据库的Service方法上添加注解 @Transactional 便可
public class ReggieApplication {
    public static void main(String[] args){
        //固定写法
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目成功启动了！");
    }
}
