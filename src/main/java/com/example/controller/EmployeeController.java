//开发时间 : 2022/10/26 14:42

package com.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.BaseContext;
import com.example.entity.Employee;
import com.example.entity.result.Result;
import com.example.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(lambdaQueryWrapper);

        //如果没有查询到则返回失败结果
        if(emp == null){
            return Result.error("账号或密码错误!");
        }

        //密码比对,如果不一致就返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return Result.error("账号或密码错误!");
        }

        //查看员工状态,如果为已禁用状态,则返回员工已禁用
        if (emp.getStatus() == 0){
            return Result.error("账号以被封禁!");
        }
        //登录成功
        request.getSession().setAttribute("employee",emp.getId());
        return Result.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logOut(HttpServletRequest request){
        //清理Session中保存的当前员工的id
        request.getSession().removeAttribute("employee");
        return Result.success("退出成功");
    }


    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @PostMapping()
    public Result<String> save(/*HttpServletRequest request,*/ @RequestBody Employee employee){
        log.info("新增员工,员工信息:{}",employee.toString());

        //设置初始密码为123456,但是要进行加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//不在这设置注册时间,更新时间和创建人id和更新人id,统一放到:MyMetaObjectHandler.java里添加(自定义元数据对象处理)
        //设置注册时间
//        employee.setCreateTime(LocalDateTime.now());
        //设置更新时间
//        employee.setUpdateTime(LocalDateTime.now());

        //获取当前登录用户的id
//        Long id = (Long) request.getSession().getAttribute("employee");
//        因为MyMetaObjectHandler.java里面不能使用HttpServletRequest,就不能获取id,所以要使用ThreadLocal传递
//         BaseContext.setCurrentId(id); //把id存入线程空间里面

        //设置创建人id
//        employee.setCreateUser(id);
        //设置更新人id
//        employee.setUpdateUser(id);
        employeeService.save(employee); //执行mybatisplus提供的添加语句
        return Result.success("新增员工成功!");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){
        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);
        //构造添加构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件,使用StringUtils需要导入:commons-lang依赖
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,lambdaQueryWrapper);
        return Result.success(pageInfo);
    }

    /**
     * 根据id修改员工账号状态信息修改加员工数据编辑
     * 因为json不支持传18位整数,所以需要在WebMvcConfig中配置格式转换,把json转换为String
     * @param employee
     * @return
     */
    @PutMapping
    public Result<String> update(/*HttpServletRequest request,*/ @RequestBody Employee employee){
        log.info(employee.toString());
        //已在过滤器中统一添加,这个可以省略
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        BaseContext.setCurrentId(empId); //把id存入线程空间里面

//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());
        employeeService.updateById(employee);
        return Result.success("账号信息修改成功");
    }

    /**
     * 根据id查询对应员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Employee> getByid(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if (employee != null){
            return Result.success(employee);
        }
        return Result.error("没有查询到对应员工信息");
    }
}
