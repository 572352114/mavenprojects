package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.omg.CORBA.portable.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 文件上次下载控制层
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    /*
    * 1.@Value(“${xxxx}”)注解从配置文件读取值的用法，也就是从application.yaml文件中获取值。
    * 2.常量注入
    *   @Value("xiaozhu")
    *   private String name;
    *   表明name的值是xiaozhou。
    * 3.@Value(“#{}”)是获取bean属性，系统属性，表达式
    * */
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info(file.toString());

        //获取原始文件名  getOriginalFilename
        String originalFilename =file.getOriginalFilename();

        /*
         先截取原始文件名格式后缀        lastIndexOf 从后往前找  例如   123.jpg  lastIndexOf(".")  返回  123
                                       substring   截取字符串   substring(123)   返回.jpg
         */
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //通过uuid处理 避免文件名重复造成文件覆盖    UUID.randomUUID()生成的格式是 UUID 所以需要通过toString 转成字符串String格式
        String filename = UUID.randomUUID().toString()+suffix;

        //判断basePath路径地址是否存在
        //先创建一个目录对象
        File dir = new File(basePath);

        //判断是否存在
        if(!dir.exists()){
            //不存在则创建   mkdirs/mkdir    mkdirs能创建多级目录
            dir.mkdirs();
        }

        //转存文件 file.transferTO
        try {
            file.transferTo(new File(basePath+filename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(filename);
    }

    /**
     * 文件下载
     * @param name
     * @param response   输出流需要通过response获得
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        //需要输入流和输入流

        try(
                //创建低级的字节输入流与源文件接通
                //                FileInputStream is = new FileInputStream(basePath+name);
                FileInputStream is = new FileInputStream(new File(basePath+name));
                //将低级的字节输入流包装成高级的缓冲字节输入流
                BufferedInputStream bis = new BufferedInputStream(is);

                //创建低级的字节输出流与目标文件接通
                //FileOutputStream os = new FileOutputStream();
                //将低级的字节输出流包装成高级的缓冲字节输出流
                //BufferedOutputStream bos = new BufferedOutputStream(os);

                /*
                * response.getOutputStream()获得字节流，通过该字节流的write(byte[] bytes)可以向response缓冲区中写入字节，
                *                                       再由Tomcat服务器将字节内容组成Http响应返回给浏览器
                * response.getWriter().write("字符串")，这个方法只能写字符串。如果要写字节，比如，传个图片，怎么办呢？就要靠response.getOutputStream()
                *
                * 通过response.getOutputStream()获得字节流无需再包装一个缓冲字节流   因为已经存在缓存了
                * */
                ServletOutputStream os = response.getOutputStream();
        ) {
            //response.setContentType 方法用于设置发送到客户端的响应的内容类型    可以百度下response.setContentType的参数类型
            response.setContentType("image/jpeg");

            byte[] bytes = new byte[1024];//定义一个字节数组转移数据
            int len;//记录每次读取的字节数
            while ((len = bis.read(bytes))!=-1){
                os.write(bytes,0,len);
                os.flush();//刷新
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
