package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Override
    @Transactional
    public void submit(Orders orders) {
        //1.获取当前用户
        Long userId = BaseContext.getCurrentId();

        //2.获取当前用户购物车中菜品/套餐数据
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        if(shoppingCartList == null || shoppingCartList.size()==0){//众所周知 前端页面所有的请求都可以被绕过  所以后端最好还是做一个校检 判断一下购物车数据为空的情况
            throw new CustomException("购物车为空，禁止结算！");
        }


        //3.完成下单（向订单orders表插入数据） 前端已有数据 address_book_id 地址id pay_method支付方式 remark 备注信息
        //3.1 设置number(订单号)
        String orderId = IdWorker.getTimeId();
        orders.setNumber(orderId);

        //3.2 设置status(订单状态) 1待付款，2待派送，3已派送，4已完成，5已取消
        orders.setStatus(2);

        //3.3 用户信息  user_id  user_name
        User user = userService.getById(userId);
        orders.setUserId(user.getId());
        orders.setUserName(user.getName());

        //3.4 设置order_time(下单时间) checkout_time(结账时间)
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());//没有开发真正的支付功能 需要资质 所以就不设置真正结算时间

        //3.5 设置amount(订单总价)  虽然前端有计算，但是能被绕过传错误金额过来  所以这里还需要我们自己再计算一遍
        //  顺便把订单明细order_detail数据插入进去（将购物车数据复制）
        /**
         * AtomicInteger：1.支持原子操作的Integer类 2.主要用于在高并发环境下的高效程序处理。使用非阻塞算法来实现并发控制
         * AtomicInteger详解：https://www.jianshu.com/p/073096a729f6
         *                   https://blog.csdn.net/weixin_42146366/article/details/87541488
         * 原子操作类详解：https://blog.csdn.net/fanrenxiang/article/details/80623884
         */
        //先定义一个Integer的原子类 设置一下默认值0
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(e ->{
            //Integer shopNumber = e.getNumber();
            //BigDecimal shopAmount = e.getAmount();
            //amount.addAndGet(shopAmount.multiply(shopAmount).intValue());
            /*解析：e.getAmount()得到的类型是BigDecimal 计算方法 加法： add()   减法：subtract()   乘法：multiply()   除法：divide()  绝对值：abs()
                 而e.getNumber()得到类型是Integer    所以需要把e.getNumber()通过new BigDecimal()方法来转成BigDecimal类型数据
                 然后amount的类型是AtomicInteger(支持原子操作的Integer类) 所以最后需要将计算结果转成 intValue()
               疑问：orders表中的amount字段类型是decimal 这里用的是AtomicInteger 不应该是AtomicDouble（好像没有这个类型） */
            amount.addAndGet(e.getAmount().multiply(new BigDecimal(e.getNumber())).intValue());

            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(e,orderDetail);
/*            orderDetail.setNumber(e.getNumber());
            orderDetail.setDishFlavor(e.getDishFlavor());
            orderDetail.setDishId(e.getDishId());
            orderDetail.setSetmealId(e.getSetmealId());
            orderDetail.setName(e.getName());
            orderDetail.setImage(e.getImage());
            orderDetail.setAmount(e.getAmount());*/
            orderDetail.setOrderNumber(orderId);
            return orderDetail;
        }).collect(Collectors.toList());
        orders.setAmount(new BigDecimal(amount.get()));//amount.get()获取amount的值
        // 插入订单明细数据
        orderDetailService.saveBatch(orderDetailList);

        //3.6 地址信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if(addressBook == null){//假如前端又被绕过 没有默认地址信息
            throw new CustomException("地址信息有误 ，禁止结算！");
        }
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(
                (addressBook.getProvinceName()==null?"":addressBook.getProvinceName()//省份
                +addressBook.getCityName()==null?"":addressBook.getCityName()//市
                +addressBook.getDistrictName()==null?"":addressBook.getDistrictName()//区
                +addressBook.getDetail()==null?"":addressBook.getDetail()//详细地址
                )
        );
        orders.setConsignee(addressBook.getConsignee());

        //3.7 插入订单数据
        this.save(orders);

        //4.下单完成后 清空当前用户的购物车数据
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }

    @Override
    @Transactional
    public void again(Orders orders) {
        //根据订单id再来一单
        Orders newOrders = this.getById(orders.getId());
        //让订单id自增
        newOrders.setId(null);
        //重新生成订单号
        String orderId = IdWorker.getTimeId();
        newOrders.setNumber(orderId);
        //重新设置一下订单时间和结算时间
        newOrders.setOrderTime(LocalDateTime.now());
        newOrders.setCheckoutTime(LocalDateTime.now());
        //设置一下订单状态
        newOrders.setStatus(2);
        this.save(newOrders);

        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(orders.getNumber()),OrderDetail::getOrderNumber,orders.getNumber());
        List<OrderDetail> list = orderDetailService.list(queryWrapper);
        //处理下 订单明细的订单号
        list.stream().forEach(e ->{
            e.setId(null);
            e.setOrderNumber(orderId);
        });
        orderDetailService.saveBatch(list);

    }
}
