package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
//@Data： 注解该类是 实体类  为类提供读写方法，从而不用写get,set,toString()方法
@Data
//Serializable接口是启用其序列化功能的接口
public class Employee implements Serializable {
    //serialVersionUID:意思是序列化的版本号
    /*如果在实现序列化接口的时候，没有显式指定一个固定值，
    java序列化机制是会自动生成一个serialVersionUID，
    这个自动值会受类名称、它所实现的接口、以及所有的共有的私有的和受保护的成员变量的影响。
    如果这些值改变，那么这个自动值也会改变。在反序列化时，便会出错*/
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;

    private Integer status;

    /*    @TableField

属性	            类型	            描述
value	            String	            数据库字段名
exist	            boolean	            exist = false 表示该属性不是数据库字段，新增等使用bean的时候，mybatis-plus就会忽略这个，不会报错
condition	        String	            预处理 where 实体查询比较条件，有值设置则按设置的值为准，没有则为默认全局的 %s=#{%s}。@TableField(condition = SqlCondition.LIKE)输出SQL为: select 表 where name LIEK CONCAT('%',值,'%')
update	            String	            预处理 update set 部分注入，例如：当在age字段上注解update=“%s+1” 表示更新时会 set age=age+1 （该属性优先级高于 el 属性）
insertStrategy	    FieldStrategy	    Mybatis-plus insert对字段的操作
updateStrategy	    FieldStrategy	    Mybatis-plus update对字段的操作
whereStrategy	    FieldStrategy	    Mybatis-plus where条件对字段的操作
fill	            FieldFill	        字段自动填充策略
select	            boolean	            @TableField(select = false) 查询时，则不返回该字段的值 。
keepGlobalFormat	boolean	            是否保持使用全局的 format 进行处理
jdbcType	        JdbcType	        JDBC 类型 (该默认值不代表会按照该值生效)
typeHandler	        Class<? extends TypeHandler>	类型处理器 (该默认值不代表会按照该值生效)
numericScale	    String	            指定小数点后保留的位数
    */
    /*
    * FieldFill
    *   DEFAULT    默认不处理
        INSERT     插入填充字段
        UPDATE     更新填充字段
        INSERT_UPDATE    插入和更新填充字段
    * */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}
