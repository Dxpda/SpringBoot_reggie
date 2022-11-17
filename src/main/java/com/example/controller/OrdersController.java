//开发时间 : 2022/11/2 11:30

package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.BaseContext;
import com.example.entity.OrderDetail;
import com.example.entity.Orders;
import com.example.entity.dto.OrdersDto;
import com.example.entity.result.Result;
import com.example.service.OrderDetailService;
import com.example.service.OrdersService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 去支付,保存订单信息,和订单详细信息,然后清空购物车
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        ordersService.submit(orders);
        return Result.success("支付成功!");
    }

    /**
     * 客户端展示订单数据
     * @return
     */
    @GetMapping("/userPage")
    public Result<Page> page(Integer page,Integer pageSize){
        //根据传过来的值进行分页
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //暂时开一个分页,后面需要把pageInfo查询到的数据给当前dtopage
        Page<OrdersDto> dtoPage = new Page<>();
        //根据userid查询当前id对应的订单
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        //将查询到的结果给到ordersPage对象
        ordersService.page(pageInfo, lambdaQueryWrapper);
        //然后再把ordersPage对象的东西拷贝到dtoPage,但是不要ordersPage里面的records属性,因为要单独设置里面的属性
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        //取出查询到的Orders数据
        List<Orders> list = pageInfo.getRecords();
        //先开一个OrdersDtoList实例对象,后面需要把数据拷贝到当前对象里面
        List<OrdersDto> dtoList = new ArrayList<>();
        for (Orders orders : list) {
            //实例一个ordersDto对象
            OrdersDto ordersDto = new OrdersDto();
            //将list集合中的单独一个orders数据拷贝到ordersDto里面
            BeanUtils.copyProperties(orders,ordersDto);
            //获取orders(订单)的id
            Long ordersId = orders.getId();
            //根据id查询
            LambdaQueryWrapper<OrderDetail> detailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            detailLambdaQueryWrapper.eq(OrderDetail::getOrderId,ordersId);
            List<OrderDetail> orderDetailList = orderDetailService.list(detailLambdaQueryWrapper);
            //将查询到的数据保存到ordersDto中
            ordersDto.setOrderDetails(orderDetailList);
            //再把orderDto保存到list集合中
            dtoList.add(ordersDto);
        }
        //把dtolist保存到dtoPage的Records中,因为之前并没有未这个属性拷贝数据,所以是空的,不会产生额外的数据
        dtoPage.setRecords(dtoList);
        return Result.success(dtoPage);
    }

    /**
     * 后端分页查询订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page1(Integer page, Integer pageSize, Long number, LocalDateTime beginTime,LocalDateTime endTime){
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.like(number != null,Orders::getNumber,number);
        //查询大于开始时间,和小于结束时间里面的数据
        // ordersLambdaQueryWrapper.gt(beginTime != null,Orders::getOrderTime,beginTime);
        // ordersLambdaQueryWrapper.lt(endTime != null,Orders::getOrderTime,endTime);
        //查询开始时间和结束时间里面的结果
        ordersLambdaQueryWrapper.between(beginTime != null&&endTime != null,Orders::getOrderTime,beginTime,endTime);
        ordersService.page(ordersPage,ordersLambdaQueryWrapper);

        Page<OrdersDto> ordersDtoPage = new Page<>();
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        //得到订单数据
        List<Orders> records = ordersPage.getRecords();
        //里面可以装订单数据和详细订单数据.....
        List<OrdersDto> ordersDtoList = new ArrayList<>();
        if (records != null){
            for (Orders record : records) {
                OrdersDto ordersDto = new OrdersDto();
                //数据拷贝
                BeanUtils.copyProperties(record,ordersDto);
                Long ordersId = record.getId();
                LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
                orderDetailLambdaQueryWrapper.eq(ordersId != null,OrderDetail::getOrderId,ordersId);
                //获取订单详细数据
                List<OrderDetail> orderDetails = orderDetailService.list(orderDetailLambdaQueryWrapper);
                ordersDto.setOrderDetails(orderDetails);
                ordersDtoList.add(ordersDto);
            }
            ordersDtoPage.setRecords(ordersDtoList);
            return Result.success(ordersDtoPage);
        }

        return Result.error("系统异常,请稍后再试!");
    }

    /**
     * 后端修改订单已完成操作
     * @return
     */
    @PutMapping
    public Result<String> ok(@RequestBody Orders orders){
        LambdaUpdateWrapper<Orders> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(orders.getId() != null,Orders::getId,orders.getId());
        lambdaUpdateWrapper.set(orders.getStatus() != null,Orders::getStatus,orders.getStatus());
        boolean update = ordersService.update(lambdaUpdateWrapper);
        if (!update){
            return Result.error("修改'订单完成'失败");
        }
        return Result.success("修改订单状态成功");
    }

}
