package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/*** 菜品控制层*/
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;
    /** 菜品列表查询显示 */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        log.info("page = {},pagesize = {},yourname = {}",page,pageSize,name);
        //构造分页构造器
        Page<Dish> pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //查询条件
        queryWrapper.like(StringUtils.isNotBlank(name),Dish::getName,name);

        //排序条件
        //queryWrapper.orderByAsc(Dish::getCategoryId,Dish::getName);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行查询
        dishService.page(pageInfo,queryWrapper);

        /**  上面的查询语句已经能返回菜品管理页面显示的大部分数据(pageInfo)   而Dish表里只有category_id
         * 需要返回一个菜品分类名称categoryName  所以需要另外处理(dishDtoPage)*/
        Page<DishDto> dishDtoPage =new Page<>();

        //对象拷贝BeanUtils.copyProperties
        // ignoreProperties 拷贝的时候忽略某些属性  Page 中的records 对应的是 protected List<T> records;
        // 相当于就是前端菜品管理页面显示的大部分数据(就是Page<Dish>下的List)忽略掉，拷贝page中的其它内容
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        //而忽略的records 需要我们自行处理(加上菜品分类名称categoryName)
        //先将之前pageInfo下的records拿过来存入List<Dish>表中
        List<Dish> list = pageInfo.getRecords();
        //再对Lish<Dish>表中的数据通过stream().map()进行处理，加上CategoryName后，收集再转为List赋值给List<DishDto>
        List<DishDto> dtoList= list.stream().map( e ->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(e,dishDto);

            Category category = categoryService.getById(e.getCategoryId());
            dishDto.setCategoryName(category.getName());

            return dishDto;
        }).collect(Collectors.toList());

        //最后再将处理后的dtoList赋值给 dishDtoPage下被拷贝忽略的records
        dishDtoPage.setRecords(dtoList);

        return R.success(dishDtoPage);
    }

    /**
     *
     * @param dishDto 数据传输对象  DishDto继承了Dish实体类
     * @return
     * 前端页面返回了DishFlavor中口味数据  而Dish实体类中没有该属性
     * 所以需要DishDto封装新的属性
     */
    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        //在业务层DishService类中定义了addDishWithFlavor方法
        //在业务控制层DishServiceImpl重写了父类DishService中的addDishWithFlavor方法
        dishService.addDishWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /** 根据菜品id 查询菜品信息*/
    @GetMapping("/{id}")//http://localhost:8080/dish/1553957071714807809
    //@PathVariable 表明这个路径变量
    public R<DishDto> getById(@PathVariable Long id){

        DishDto dishDto = dishService.getDishWithFlavor(id);

        return R.success(dishDto);
    }

    /** 更新菜品信息*/
    @PutMapping
    public R<String> updateDish(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateDishWithFlavor(dishDto);
        return R.success("修改成功！");
    }


    /** 菜品列表查询-新增/修改套餐时 */
    @GetMapping("/list")
    //public R<List<Dish>> list(Dish dish){
    //将返回数据改为DishDto 是为了手机端显示能显示菜品口味信息(手机端请求数据的接口是这个所以得调整/除非重新写个调用接口)
    public R<List<DishDto>> list(Dish dish){
        Long categoryId= dish.getCategoryId();

        String name = dish.getName();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //getStatus：1  起售   0   停售
        queryWrapper.eq(Dish::getStatus,1);
        if(categoryId!=null) {
            //queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
            queryWrapper.eq(Dish::getCategoryId, categoryId);
        }
        if(StringUtils.isNotBlank(name)){
            //queryWrapper.like(StringUtils.isNotBlank(name),Dish::getName,name);
            queryWrapper.like(Dish::getName,name);

        }

        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        //新增手机端显示菜品口味信息
        List<DishDto> dtoList = list.stream().map(e ->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(e,dishDto);

            Category category = categoryService.getById(e.getCategoryId());
            dishDto.setCategoryName(category.getName());

            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,e.getId());
            dishDto.setFlavors(dishFlavorService.list(dishFlavorLambdaQueryWrapper));

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dtoList);
    }

    @DeleteMapping
    public R<String> deleteDish(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        dishService.deleteDishWithFlavor(ids);

        return R.success("删除成功！");
    }

    @PostMapping("/status/0")
    public R<String> stopDish(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        dishService.stopWithStart(ids,0);

        return R.success("操作成功！");
    }

    @PostMapping("/status/1")
    public R<String> startDish(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        dishService.stopWithStart(ids,1);

        return R.success("操作成功！");
    }

}
