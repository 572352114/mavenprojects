package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void addSetmealWithDish(SetmealDto setmealDto);
    public SetmealDto getSetmealWithDish(Long id);
    public void updateSetmealWithDish(SetmealDto setmealDto);
    public void deleteSetmealWithDish(List<Long> ids);
    public void stopWithStart(List<Long> ids,int status);
}
