package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Value("${reggie.path}")
    private String basePath;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    //因为DishServiceImpl 实现了 DishService 接口 所以这里就不需要声明 private DishService dishService
/*    @Autowired
    private DishService dishService;*/

    @Override
    @Transactional
    //* 声明式事务管理   应该只被应用到 public 方法上,这是由 Spring AOP 的本质决定的
    //* @Transactional详解：https://blog.csdn.net/jiangyu1013/article/details/84397366
    public void addDishWithFlavor(DishDto dishDto) {
        this.save(dishDto);//保存菜品表

        //保存菜品口味之前需要将菜品id 存到dish_flavor中的 dish_id属性中
        Long dishId= dishDto.getId();
        //创建DishFlavor 的List集合 使用Stream流操作 修改集合中每个DishFlavor元素的DishId值
        List<DishFlavor> dishDtoList = dishDto.getFlavors().stream().map( e ->{
            e.setDishId(dishId);
            return e;
        } ).collect(Collectors.toList());
        /**
         Stream流的常用API
         forEach : 逐一处理(遍历)
         count：统计个数
         -- long count();
         filter : 过滤元素
         -- Stream<T> filter(Predicate<? super T> predicate)
         limit : 取前几个元素
         skip : 跳过前几个
         sorted : 排序
         map : 加工方法
         concat : 合并流
         collect(Collectors.toList()):收集Stream流的数据到 集合或者数组中去。
         */

        //保存菜品口味数据到菜品口味表dish_flavor    saveBatch批量保存
        dishFlavorService.saveBatch(dishDtoList);
    }

    @Override
    public DishDto getDishWithFlavor(Long id) {
        //先通过接口的getById方法得到dish对象
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        //将dish对象中的属性拷贝给dishDto     dishDto继承了dish  又扩展了几个属性  Flavors(代表菜品口味的集合)
        BeanUtils.copyProperties(dish,dishDto);

        //因为getByid方法只能通过查询 dish_flavor 中的id来匹配   而现在传入进来的是 dish_flavor 中的dish_id
/*        DishFlavor dishFlavor = dishFlavorService.getById(id);*/

        //所以需要构造条件构造器来查询菜品口味（dish_flavor）表中的信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(DishFlavor::getDishId,id);

        //将查询结果存入List<DishFlavor> 表中
        List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);

        //再赋值 dishDto中的 Flavors属性
        dishDto.setFlavors(dishFlavorList);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateDishWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);
        Long dishId = dishDto.getId();
        //先清理菜品口味表中的数据 再重新添加

        //删除
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(DishFlavor::getDishId,dishId);
        dishFlavorService.remove(queryWrapper);
        //添加
        List<DishFlavor> dishDtoList = dishDto.getFlavors().stream().map( e -> {
            e.setDishId(dishId);
            return e;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(dishDtoList);
    }

    @Override
    @Transactional
    public void deleteDishWithFlavor(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids).eq(Dish::getStatus,1);
        int count = this.count(queryWrapper);
        if(count>0){
            throw  new CustomException("该菜品正在售卖中！禁止删除！");
        }
        //新增 当该菜品有被套餐使用时(不管该套餐是否正在售卖)  不允许删除
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getDishId,ids);
        int count1 = setmealDishService.count(queryWrapper1);
        if(count1>0){
            throw  new CustomException("该菜品正在某套餐中售卖！禁止删除！(请先删除该套餐中的该菜品再来删除该菜品！)");
        }

        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(lambdaQueryWrapper);

        for (Long id : ids) {
            Dish dish = this.getById(id);
            File dir = new File(basePath+dish.getImage());
            dir.delete();
        }
        this.removeByIds(ids);
    }

    @Override
    public void stopWithStart(List<Long> ids,int status) {
        for (Long id : ids) {
            Dish dish = this.getById(id);
            dish.setStatus(status);
            this.updateById(dish);
        }
    }
}
