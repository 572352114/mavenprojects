package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Value("${reggie.path}")
    private String basePath;

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void addSetmealWithDish(SetmealDto setmealDto) {
        //1.先保存套餐信息
        this.save(setmealDto);

        //2.再保存套餐的菜品信息  主要是存入套餐id
        Long setmealId = setmealDto.getId();
        setmealDto.getSetmealDishes().stream().forEach(e -> e.setSetmealId(setmealId));
        setmealDishService.saveBatch(setmealDto.getSetmealDishes());
    }

    @Override
    public SetmealDto getSetmealWithDish(Long id) {
        //1.先根据套餐id查询套餐信息 存入setmeal对象中
        Setmeal setmeal = this.getById(id);

        //2.将setmeal对象中的属性拷贝到 setmealDto对象中去
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //3.再根据套餐id查询套餐下对应的菜品信息  存入到 setmealDto对象中的setmealDishes(list集合)属性中去
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(SetmealDish::getSetmealId,id);

        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    @Override
    @Transactional
    public void updateSetmealWithDish(SetmealDto setmealDto) {
        //1.先更新套餐信息
        this.updateById(setmealDto);
        //2.再根据套餐id  先删除之前的菜品信息
        Long setmealId = setmealDto.getId();
        //setmealDishService.removeById(setmealId);   removeById只能根据SetmealDish表中的id来删除  而现在传进来的是setmeal_id
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        setmealDishService.remove(queryWrapper);

        //3.再保存新的菜品信息
        setmealDto.getSetmealDishes().stream().forEach(e -> e.setSetmealId(setmealId));
        setmealDishService.saveBatch(setmealDto.getSetmealDishes());

    }

/*
    @Override
    @Transactional
    public void deleteSetmealWithDish(List<Long> ids) {

        for (Long id : ids) {
            //1.先查询套餐信息 是否停售Status：1  起售   0   停售
            Setmeal setmeal = this.getById(id);
            if(setmeal.getStatus()==0){
                //2.先删除套餐下的菜品信息
                LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SetmealDish::getSetmealId,id);
                setmealDishService.remove(queryWrapper);
            }else {
                R.error("该套装正在售卖中，不允许删除！");
            }
        }
        this.removeByIds(ids);
    }*/
    //优化之后：先判断List集合整体是否有起售套餐  有一个则抛出异常 不允许删除
    @Override
    @Transactional
    public void deleteSetmealWithDish(List<Long> ids) {
        //1.做整体判断
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids).eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);//统计个数
        if(count>0){
            throw new CustomException("套餐正在售卖中，不允许删除！");
        }

        //2.先删除套餐下的菜品信息
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //2.1使用for循环
/*        for (Long id : ids) {
            setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
            setmealDishService.remove(setmealDishLambdaQueryWrapper);
        }*/
        //2.2 查询所有该套餐下的菜品id  统一删除
/*        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);*/

        //3.删除套餐 的同时需要删除img下的文件
        for (Long id : ids) {
            Setmeal setmeal =  this.getById(id);
            File dir = new File(basePath+setmeal.getImage());
            dir.delete();
        }
        this.removeByIds(ids);
    }

    /** 修改套餐状态  起售/停售*/
    @Override
    public void stopWithStart(List<Long> ids, int status) {
/*        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids).eq(Setmeal::getStatus,status);
        int count = this.count(queryWrapper);
        if(count>0){
            if(status==0){
                throw new CustomException("所选套餐状态是停售，操作失败！");
            }else{
                throw new CustomException("所选套餐状态是起售，操作失败！");
            }
        }else {
            for (Long id : ids) {
                Setmeal setmeal =  this.getById(id);
                setmeal.setStatus(status);
                this.updateById(setmeal);
            }
        }*/
        //如果不需要判断套餐状态  直接全部修改
        for (Long id : ids) {
            Setmeal setmeal =  this.getById(id);
            setmeal.setStatus(status);
            this.updateById(setmeal);
        }

    }
}
