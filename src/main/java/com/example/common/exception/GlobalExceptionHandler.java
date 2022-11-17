//开发时间 : 2022/10/27 10:38

package com.example.common.exception;

import com.example.entity.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
//表示处理RestController 和Controller里面发出的异常
@ControllerAdvice(annotations = {RestController.class, Controller.class} )
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 异常处理方法
     * @param e
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException e){
        log.error(e.getMessage());
        //contains(),判断属性值是否包含指定的数据
        if (e.getMessage().contains("Duplicate entry")){
            //根据空格分隔字符串,因为返回的错误是: Duplicate entry 'zhangsan' for key 'employee.idx_username'
            String[] split = e.getMessage().split(" ");
            return Result.error("失败了,"+split[2]+"已存在!");
        }
        return Result.error("后台出现异常,请稍后再试!");
    }

    /**
     * 处理自定义的异常方法
     * @param e
     * @return
     */
    @ExceptionHandler(CustomerException.class)
    public Result<String> customerException(CustomerException e){
        return Result.error(e.getMessage());
    }

}

