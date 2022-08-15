package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartContrller {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /** 新增菜品/套餐   */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("数据：{}",shoppingCart.toString());
        //1.设置用户id  表明是哪个用户添加的
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //2.获取当前菜品/套餐id
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        //2.判断当前用户  添加的菜品/套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        //如果菜品存在 并且口味一样  则number+1 否则添加到购物车中
        if(dishId!=null){
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
            //前端不支持不同口味的菜品添加 -_-!  添加了一份菜品之后  该菜品就只支持同一种口味 所以就不用考虑不同口味的情况
            //queryWrapper.eq(ShoppingCart::getDishFlavor,shoppingCart.getDishFlavor());

        }
        //如果套餐存在 则直接number+1 否则添加到购物车中
        if(setmealId!=null){
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);

        }
/*        //错误（提醒） 这里使用Count的话   假如购物车中存在数据 Count只会是1  所以这里要先整ShoppingCart对象 判断有无 再number+1
        int Count = shoppingCartService.count(queryWrapper);
        if(Count==0){
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
        }else{
            shoppingCart.setNumber(Count+1);
            shoppingCartService.updateById(shoppingCart);
        }*/
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);
        if(shoppingCartOne != null){
            //Integer number = shoppingCartOne.getNumber();
            //shoppingCart.setNumber(number+1);
            //错误（提醒）shoppingCartOne.getNumber()+1不行   但是shoppingCartOne.getNumber()返回值类型是Integer 不懂为啥不会+1
            //错误（大悟）哈哈 之前是shoppingCart.setNumber(shoppingCartOne.getNumber()+1); 找错对象了 所以不会+1
            shoppingCartOne.setNumber(shoppingCartOne.getNumber()+1);
            shoppingCartOne.setCreateTime(LocalDateTime.now());
            shoppingCartService.updateById(shoppingCartOne);
        }else{
            //shoppingCart.setNumber(1);数据库shopping_cart表中的number字段 已设置默认值 1
            shoppingCart.setNumber(1);//返回给前端页面需要number 所以还是得设置一下
            //由于实体类ShoppingCart中的create_time属性没有加@TableField(fill = FieldFill.INSERT_UPDATE) 所以需要自行添加
            //如果实体类ShoppingCart中的create_time属性加上了@TableField(fill = FieldFill.INSERT_UPDATE) 但是自动填充的时候 会因为找不到update_time而报错
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //处理一下 统一返回shoppingCartOne 或者在上面将 shoppingCart=shoppingCartOne 也行
            shoppingCartOne=shoppingCart;
        }
        return R.success(shoppingCartOne);
    }

    /** 获取当前用户的购物车信息*/
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        Long uesrId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,uesrId);
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCart = shoppingCartService.list(queryWrapper);
        return R.success(shoppingCart);
    }

    /** 删除菜品/套餐 */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        log.info("数据：{}",shoppingCart.toString());
        //1.先去购物车中查询当前用户 是否已经存在该菜品/套餐  存在 则number-1(当number=1时  直接删除)
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        //2.获取当前菜品/套餐id
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        if(dishId!=null){
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }
        if(setmealId!=null){
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);
        if(shoppingCartOne != null){
            int number = shoppingCartOne.getNumber();
            if(number == 1){
                shoppingCartService.remove(queryWrapper);
            }else{
                shoppingCartOne.setNumber(number-1);
                shoppingCartService.updateById(shoppingCartOne);
            }
        }
        return R.success(shoppingCartOne);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空成功！");
    }
}
