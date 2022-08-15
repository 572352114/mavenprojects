package com.itheima.reggie.config;


import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

//@Configuration  告诉Spring boot 这是一个配置类
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /*
    *   registry : 设置静态资源映射
    *   注意：如果配置这个类的话   原默认static下的访问路径会失效！
    * */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始资源映射！");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:front/");
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器！");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter= new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将java对象转换为Json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将消息转换器对象追加到MVC框架的转换器集合中去  注意：index：0表示索引在最前面，优先使用该转换器
        converters.add(0,messageConverter);

        //super.extendMessageConverters(converters);
    }
}
