package com.itheima.reggie.common;

/**
 * 在业务层实现类CategoryServiceImpl 中 重构了remove方法  * 删除分类时 有关联菜品/套餐 异 常
 * 在业务层实现类SetmealServiceImpl 中 重构了remove方法   * 删除起售套餐  异常
 * 在业务层实现类DishServiceImpl 中 重构了remove方法   * 删除起售菜品    异常
 *
 */
public class CustomException extends RuntimeException{
    public CustomException() {
    }

    public CustomException(String message) {
        super(message);
    }
}
