package com.example;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.Employee;
import com.example.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReggieTakeOutApplicationTests {
    @Autowired
    private EmployeeService employeeService;

    @Test
    void contextLoads() {
       String a = "12444.jsp";
        String[] substring = a.split("\\.");
        System.out.println(substring[1]);
    }

}
