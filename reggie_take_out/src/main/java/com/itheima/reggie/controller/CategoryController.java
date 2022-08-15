package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 分类控制层
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /*** 新增分类*/
    @PostMapping
    public R<String> addCategory(@RequestBody Category category){
        categoryService.save(category);

        log.info("新增分类！分类信息：{}",category.toString());
        return R.success("新增分类成功！");
    }

    /*** 修改分类*/
    @PutMapping
    public R<String> updateCategory(@RequestBody Category category){
        log.info("分类信息：{}",category.toString());
        categoryService.updateById(category);
        return R.success("修改成功！");
    }

    /*** 删除分类*/
    @DeleteMapping
    public R<String> deleteCategory(Long ids){
        log.info("删除分类,id：{}",ids);

        //categoryService.removeById(ids);
        //上面是直接通过id删除分类
        //下面是重写了remove方法 做了判断 该分类是否关联了菜品/套餐 有关联则抛出自定义CategoryAssociateException异常 再被GlobalExceptionHandler全局异常拦截器器捕获处理
        categoryService.remove(ids);

        return R.success("删除成功！");
    }

    /*** 分类分页查询*/
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        log.info("page={}, pagesize={}",page,pageSize);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();

        //queryWrapper.eq(Category::getIsDeleted,0);

        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort,Category::getType);

        //执行查询
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /*** 分类列表查询*/
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper =new LambdaQueryWrapper();
        //添加查询条件    先判断type不为空
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //执行查询
        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);
    }

}
