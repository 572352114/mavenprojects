package com.atguigu.boot.controller;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/*
    @ResponseBody 详解：https://blog.csdn.net/originations/article/details/89492884
作用是将controller的方法返回的对象通过适当的转换器转换为指定的格式之后，写入到response对象的body区，通常用来返回JSON数据或者是XML数据
@ResponseBody
@Controller
   @RestController=@ResponseBody+@Controller
*/
@RestController
public class HelloWorldController {
    @RequestMapping("/hello")
    public String handle01(){
        System.out.println("ni hao!");
        return "Hello, Spring boot 2!";
    }

}
