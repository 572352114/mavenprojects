package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    /** 手机端查询套餐*/
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus,1);
        queryWrapper.orderByAsc(Setmeal::getId);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    /** 手机端查询套餐菜品*/
    @GetMapping("/dish/{id}")
    public R<List<SetmealDto>> dish(@PathVariable Long id){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        queryWrapper.orderByAsc(SetmealDish::getDishId);
        List<SetmealDish> dishList = setmealDishService.list(queryWrapper);

        List<SetmealDto> list = dishList.stream().map(e ->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(e,setmealDto);

            Dish dish = dishService.getById(e.getDishId());
            setmealDto.setImage(dish.getImage());
            return setmealDto;
        }).collect(Collectors.toList());
        return R.success(list);
    }

    /** 查询套餐列表*/
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pagesize = {},yourname = {}",page,pageSize,name);

        Page<Setmeal> setmealPage =new Page<>(page,pageSize);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        //记得先取判断传过来的name是否为空
        queryWrapper.like(StringUtils.isNotBlank(name), Setmeal::getName,name);
        //queryWrapper.like(Setmeal::getName,name);

        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(setmealPage,queryWrapper);

        Page<SetmealDto> setmealDtoPage =new Page<>();

        //提醒（之前犯的错误）：赋值 忽略的是  Page<T> 下的属性records  而不是 SetmealDto下的属性setmealDishes
        //BeanUtils.copyProperties(setmealPage,setmealDtoPage,"setmealDishes");
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        List<Setmeal> list = setmealPage.getRecords();

        List<SetmealDto> dtoList = list.stream().map( e -> {
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(e,setmealDto);

            Category category = categoryService.getById(e.getCategoryId());

            setmealDto.setCategoryName(category.getName());

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(dtoList);

        return R.success(setmealDtoPage);
    }

    /** 新增套餐*/
    @PostMapping
    public R<String> addStemeal(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());

        setmealService.addSetmealWithDish(setmealDto);

        return R.success("新增成功！");
    }

    /** 根据id查询套餐*/
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){

        SetmealDto setmealDto = setmealService.getSetmealWithDish(id);

        return R.success(setmealDto);
    }

    /** 修改套餐信息*/
    @PutMapping
    public R<String> updateSetmeal(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());

        setmealService.updateSetmealWithDish(setmealDto);

        return R.success("修改成功！");
    }

    /** 删除/批量删除 套餐*/
    @DeleteMapping    //http://localhost:8080/setmeal?ids=1555108633094914050,1415580119015145474
    //@RequestParam  多个数据之间使用逗号分隔开，在后台接口中可以使用数组或者list类型的变量来接收
    //详解：https://blog.csdn.net/qq_43842093/article/details/121175080
    public R<String> deleteSetmeal(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.deleteSetmealWithDish(ids);
        return null;
    }

    /** 停售/批量停售 套餐*/
    @PostMapping("/status/0")
    public R<String> stopSetmeal(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.stopWithStart(ids,0);
        return R.success("操作成功！");
    }

    /** 起售/批量起售 套餐*/
    @PostMapping("/status/1")
    public R<String> startSetmeal(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.stopWithStart(ids,1);
        return R.success("操作成功！");
    }

}
