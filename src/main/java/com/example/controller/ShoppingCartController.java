//开发时间 : 2022/11/3 9:28

package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.common.BaseContext;
import com.example.entity.ShoppingCart;
import com.example.entity.result.Result;
import com.example.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 客户端展示个人购物车里面的商品
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartService.list(lambdaQueryWrapper);
        return Result.success(list);
    }

    /**
     * 客户端为购物城添加菜品或套餐
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<Integer> add(@RequestBody ShoppingCart shoppingCart){
        Long currentId = BaseContext.getCurrentId();
        //把客户的id放进去
        shoppingCart.setUserId(currentId);
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //判断添加的是套餐还是菜品
        if (shoppingCart.getDishId() != null){
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else{
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(lambdaQueryWrapper);
        //判断当前菜品是否已经添加了
        if (one != null){
            //如果添加了,那么就给当前数据的数量加1
            one.setNumber(one.getNumber()+1);
            shoppingCartService.updateById(one);
        }else{
            //如果不存在,那么就添加数据
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }
        return Result.success(one.getNumber());
    }


    /**
     * 客户端购物车商品的数量减少操作
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public Result<Integer> sub(@RequestBody ShoppingCart shoppingCart){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //指定查询客户对应id的数据
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        //判断getDishId是否为null,如果不为null就查询Dish的对应的商品数据数据,如果为空就查询Setmeal对应的商品数据
        if (shoppingCart.getDishId() != null){
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else{
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //得到查询到的数据
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(lambdaQueryWrapper);
        LambdaUpdateWrapper<ShoppingCart> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        //判断当前数据是否小于0,如果等于0就执行删除操作
        shoppingCart1.setNumber(shoppingCart1.getNumber()-1);
        if (shoppingCart1.getNumber() == 0){
            //如果数量为0则直接删除
            LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            //因为如果当前商品为1了还点击减少,那么肯定使用删除它了
            lambdaQueryWrapper1.eq(ShoppingCart::getNumber, 1);
            shoppingCartService.remove(lambdaQueryWrapper1);
            return Result.success(shoppingCart1.getNumber());
        }else {
            //判断得到的数据是菜品数据还是套餐数据
            if (shoppingCart1.getDishId() != null){
                lambdaUpdateWrapper.eq(ShoppingCart::getDishId,shoppingCart1.getDishId());
            }else{
                lambdaUpdateWrapper.eq(ShoppingCart::getSetmealId,shoppingCart1.getSetmealId());
            }
            lambdaUpdateWrapper.set(ShoppingCart::getNumber,shoppingCart1.getNumber());
            //修改数据
            shoppingCartService.update(lambdaUpdateWrapper);
            return Result.success(shoppingCart1.getNumber());
        }
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("clean")
    public Result<String> delete(){
        Long id = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,id);
        shoppingCartService.remove(lambdaQueryWrapper);
        return Result.success("清空购物车成功!");
    }


}
