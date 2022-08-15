package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/*@ControllerAdvice：
* ControllerAdvice本质上是一个Component，因此也会被当成组建扫描
* 这个类是为那些声明了（@ExceptionHandler、@InitBinder 或 @ModelAttribute注解修饰的）方法的类而提供的专业化的@Component , 以供多个 Controller类所共享。
说白了，就是aop思想的一种实现，你告诉我需要拦截规则，我帮你把他们拦下来，具体你想做更细致的拦截筛选和拦截之后的处理，你自己通过@ExceptionHandler、@InitBinder 或 @ModelAttribute这三个注解以及被其注解的方法来自定义。
*
* 初定义拦截规则：
ControllerAdvice 提供了多种指定Advice规则的定义方式，默认什么都不写，则是Advice所有Controller
当然你也可以通过下列的方式指定规则
1.比如对于 String[] value() default {} , 写成@ControllerAdvice("org.my.pkg") 或者 @ControllerAdvice(basePackages="org.my.pkg"), 则匹配org.my.pkg包及其子包下的所有Controller
2.当然也可以用数组的形式指定，如：@ControllerAdvice(basePackages={"org.my.pkg", "org.my.other.pkg"}),
3.也可以通过指定注解来匹配，比如我自定了一个 @CustomAnnotation 注解，我想匹配所有被这个注解修饰的 Controller, 可以这么写：@ControllerAdvice（annotations={CustomAnnotation.class})
*/
//这里是匹配被@RestController和@Controller注解修饰的类
//原文链接：https://blog.csdn.net/qq_36829919/article/details/101210250
//@ControllerAdvice的介绍以及三种用法
/**
 * 全局异常拦截器
 */
@Slf4j
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
//返回JSON格式  需要@ResponseBody注解
//接收JSON格式  需要@ResquestBody注解
public class GlobalExceptionHandler {
    /*@ExceptionHandler注解我们一般是用来自定义异常的。
        可以认为它是一个异常拦截器（处理器）
     原文链接：https://blog.csdn.net/tolode/article/details/103263528
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)//申明捕获哪个异常类
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception){
        log.info(exception.getMessage());

        if(exception.getMessage().contains("Duplicate entry")){
            String[] split= exception.getMessage().split(" ");
            String msg = split[2]+" 用户名已存在";
            return R.error(msg);
        }

        return R.error("失败！");
    }

    @ExceptionHandler(CustomException.class)//申明捕获哪个异常类
    public R<String> exceptionHandler(CustomException exception){
        log.info(exception.getMessage());
        return R.error(exception.getMessage());
    }
}
