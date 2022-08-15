package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /** 手机端验证码发送*/
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //1.先获取手机号
        String phone = user.getPhone();
        //1.1    判断手机号是否为空
        if(StringUtils.isNotBlank(phone)){
            //2.生成随机的4/6位验证码    调用utils文件夹下的ValidateCodeUtils工具类
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);

            //3.调用阿里云提供的短信服务API完成发送短信   utils文件夹下的SMSUtils    需要企业认证  开通不了
            // SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //4.将生成的验证码保存到session中  等待登录时调用
            session.setAttribute(phone,code);

            return R.success("验证码短信发送成功");
        }
        return R.error("验证码短信发送失败");
    }

    /** 手机端登录*/
    @PostMapping("/login")
    //public R<String> userLogin(@RequestBody User user, HttpSession session){
    //User这个实体类 没有code验证码属性    两种方法：一是 新建一个UserDto类 继承User 再扩展code属性
    //二是 使用Map 键值对来接收参数
    public R<User> userLogin(@RequestBody Map map, HttpSession session){
        log.info("Map:{}",map.toString());

        //这里不能用Integer接收 是因为  此方法接收的Map<K,V>未定义K,V  所以都是Object类型的参数
        //Integer phone1 = map.get("phone");

        //1.获取手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        //2.从session中获取保存的验证码
        String sessionCode = session.getAttribute(phone).toString();

        //3.进行验证码的对比
        if(sessionCode != null && sessionCode.equals(code)){
            //4.判断当前手机号是否是新用户，如果是 自动注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user == null){
                //这里可以直接用user  就不用再new一个对象了
                user= new User();
                //User newUser = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //5.将userId存入session中
            session.setAttribute("user",user.getId());
            return R.success(user);
        }

        return R.error("验证码错误！登录失败！");
    }



}
