package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品的同时 需要插入菜品对应的口味数据，需要操作两张表：Dish,DishFlavor
    public void addDishWithFlavor(DishDto dishDto);

    //根据菜品id查询菜品信息和菜品口味信息
    public DishDto getDishWithFlavor(Long id);

    //更新菜品的同时 需要更新菜品对应的口味数据，需要操作两张表：Dish,DishFlavor
    public void updateDishWithFlavor(DishDto dishDto);

    //删除菜品的同时 需要删除菜品对应的口味数据，需要操作两张表：Dish,DishFlavor
    public void deleteDishWithFlavor(List<Long> ids);

    public void stopWithStart(List<Long> ids,int status);


}
