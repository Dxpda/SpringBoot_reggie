//开发时间 : 2022/11/2 11:28

package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.OrderDetailDao;
import com.example.entity.OrderDetail;
import com.example.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailDao,OrderDetail> implements OrderDetailService {
}
