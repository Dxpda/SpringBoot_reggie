//开发时间 : 2022/10/28 15:12

package com.example.common.exception;

/**
 * 自定义业务异常
 */
public class CustomerException extends RuntimeException{
    public CustomerException(String message){
        super(message);
    }
}
