package com.itheima.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
* 配置MybatisPlus的分页插件
* */
//配置类需要加 @Configuration 注解 来告诉Spring boot 这是一个配置类
@Configuration
public class MybatisPlusConfig {

    @Bean
    //表示方法产生一个由Spring管理的bean
    //@Bean方法在@Configuration 类中声明。因此，在此模式下，不能将 @Configuration类及其工厂方法标记为final或private
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }

}
