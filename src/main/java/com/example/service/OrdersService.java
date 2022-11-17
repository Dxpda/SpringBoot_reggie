package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Orders;

public interface OrdersService extends IService<Orders> {

    /**
     * 支付功能
     */
    public void submit(Orders orders);
}
