package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 */
public class BaseContext {
    //ThreadLocal<T>是与线程绑定的一个变量，该变量对其他线程而言是隔离的，也就是说该变量是当前线程独有的变量
    //一个线程变量只能存入一个值     要不就创建多个线程变量  要不就将需要存入的值做成一个集合
    private static final ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
