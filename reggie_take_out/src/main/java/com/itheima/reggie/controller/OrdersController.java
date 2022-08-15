package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /** 用户下单*/
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){

        ordersService.submit(orders);

        return R.success("下单成功！");
    }

    /** 用户下单*/
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){

        ordersService.again(orders);

        return R.success("操作成功！");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String  number,String beginTime,String endTime ){
        log.info("page = {},pagesize = {},number = {},beginTime = {},endTime = {}",page,pageSize,number,beginTime,endTime);

        Page<Orders> pageInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件 订单号 模糊查询  记得先判断订单号和查询时间段是否为空
        queryWrapper.like(StringUtils.isNotBlank(number),Orders::getNumber,number);
        queryWrapper.between(StringUtils.isNotBlank(beginTime),Orders::getOrderTime,beginTime,endTime);
        //排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        ordersService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> delivery(@RequestBody Orders orders){
        log.info(orders.toString());
        ordersService.updateById(orders);
        return R.success("操作成功！");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize){
        Page<Orders> pageInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo,queryWrapper);

        Page<OrdersDto> ordersDtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        List<Orders> ordersList = pageInfo.getRecords();
        List<OrdersDto> list = ordersList.stream().map(e -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(e,ordersDto);

            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderNumber,e.getNumber());

            List<OrderDetail> orderDetailList = orderDetailService.list(lambdaQueryWrapper);

            ordersDto.setOrderDetails(orderDetailList);

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(list);

        return R.success(ordersDtoPage);
    }



}
