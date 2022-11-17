//开发时间 : 2022/11/2 11:27

package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.BaseContext;
import com.example.common.exception.CustomerException;
import com.example.dao.OrdersDao;
import com.example.entity.*;
import com.example.service.*;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersDao, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 把购买数据添加到订单表和订单详细表,然后把购物车清空
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获取当前用户的id
        Long userId = BaseContext.getCurrentId();

        //获取购物车数据
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> list = shoppingCartService.list(lambdaQueryWrapper);
        if (list == null){
            throw new CustomerException("购物车数据有误!");
        }
        //获取总金额
        BigDecimal totalAmount = BigDecimal.valueOf(0);
        for (ShoppingCart shoppingCart : list) {
            Integer number = shoppingCart.getNumber();
            BigDecimal amount = shoppingCart.getAmount();
            totalAmount = totalAmount.add( amount.multiply(BigDecimal.valueOf(number)));//multipy:乘法运算,add:加法运算,得到单个商品的总价格
        }

        //获取用户数据
        User userById = userService.getById(userId);
        if (userById == null){
            throw new CustomerException("用户数据异常!");
        }
        //获取地址信息
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBookById = addressBookService.getById(addressBookId);
        if (addressBookById == null){
            throw new CustomerException("地址信息获取异常!");
        }
        //向订单表插入数据
        long id = IdWorker.getId();//生成id
        orders.setNumber(String.valueOf(id));//生成订单号
        orders.setStatus(3);//订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
        orders.setUserId(userId);//下单用户id
        orders.setAddressBookId(addressBookId);//地址id
        orders.setOrderTime(LocalDateTime.now());//下单时间
        orders.setCheckoutTime(LocalDateTime.now());//结账时间
//        orders.setPayMethod(1);//下单方式,1.微信,2.支付宝,前端已经传值
        orders.setAmount(totalAmount);//总金额
        orders.setPhone(addressBookById.getPhone());//收货人手机号
        orders.setUserName(userById.getName());//登录的用户名字
        orders.setConsignee(addressBookById.getConsignee());//收货人名字
        orders.setAddress((addressBookById.getProvinceName() == null ? "":addressBookById.getProvinceName())
                +(addressBookById.getCityName() == null ? "" : addressBookById.getCityName())
                +(addressBookById.getDistrictCode() == null ? "" :addressBookById.getDistrictCode())
                +(addressBookById.getDetail() == null ? "" :addressBookById.getDetail())
                );
        this.save(orders);

        //向订单明细表插入数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail,"id","createTime","userId");
            orderDetail.setOrderId(orders.getId());//获取订单id
            orderDetails.add(orderDetail);
        }
        orderDetailService.saveBatch(orderDetails);

        System.out.println(list);
        //清空购物车
        for (ShoppingCart shoppingCart : list) {
            shoppingCartService.removeById(shoppingCart);
        }
    }
}
