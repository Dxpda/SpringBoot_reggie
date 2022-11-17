//开发时间 : 2022/11/1 17:59

package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.BaseContext;
import com.example.entity.User;
import com.example.entity.result.Result;
import com.example.service.UserService;
import com.example.utils.SMSUtils;
import com.example.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.LambdaConversionException;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public Result<String> senMsg(@RequestBody User user, HttpServletRequest request){
        //获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            System.out.println("手机号是:"+phone+"\t密码是:"+code);
            //调用阿里云提供的短信服务API完成发送短信,但是要充钱......
//            SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);

            //需要将生成的验证码保存到Session
            request.getSession().setAttribute("phone",phone);
            request.getSession().setAttribute("code",code);
        }
        return Result.success("登录成功!");
    }

    /**
     * 登录操作
     * @param map
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map,HttpServletRequest request){
        //获取手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        //从session中取出数据进行比对
        String phone1 = (String) request.getSession().getAttribute("phone");
        String code1 = (String) request.getSession().getAttribute("code");

        //获取验证码后就清除,以免暴力破解
        request.getSession().removeAttribute("phone");
        request.getSession().removeAttribute("code");

        if (code1 != null && code1.equals(code) && phone1 != null && phone1.equals(phone)){
            //如果验证码比对的上就查看手机号是否存在
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone,phone);
            User one = userService.getOne(lambdaQueryWrapper);

            if (one == null){
                one = new User();
                //如果用户不存在就直接注册
                one.setPhone(phone);
                userService.save(one);
            }
            //把id存到session中
            request.getSession().setAttribute("id",one.getId());
            return Result.success(one);
        }
        return Result.error("验证码错误");
    }

    /**
     * 退出登录
     * @return
     */
    @PostMapping("/loginout")
    public Result<String> loginOut(HttpServletRequest request){
        request.getSession().removeAttribute("id");
        return Result.success("退出成功");
    }

}
