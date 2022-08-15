package com.itheima.reggie.common;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

//@Data： 注解该类是 实体类  为类提供读写方法，从而不用写get,set,toString()方法
@Data

/*
* 通过返回结果，服务器响应的数据最终都会封装成此对象
* */
public class R<T> {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map map = new HashMap(); //动态数据

    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
