//开发时间 : 2022/10/28 11:23

package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.exception.CustomerException;
import com.example.dao.CategoryDao;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.entity.Setmeal;
import com.example.service.CategoryService;
import com.example.service.DishService;
import com.example.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    /**
     * 根据id删除分类,删除之前需要进行判断
     * @param id
     */
    @Override
    public void remover(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count = dishService.count(dishLambdaQueryWrapper);
        //查询当前分类是否关联了菜品,如果已经关联,抛出一个业务异常
        if (count > 0){
            //已经关联菜品,抛出一个业务异常,然后使用GlobalExceptionHandler.java来处理
            throw new CustomerException("当前分类下关联了菜品,不能删除");
        }
        //查询轮当前套餐是否关联了套餐,如果已经关联,抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count1 = setmealService.count(setmealLambdaQueryWrapper);
        if (count1 > 0){
            //已经关联到了套餐,抛出一个业务异常
            throw new CustomerException("当前分类关联了套餐,不能删除");
        }

      super.removeById(id);
    }
}
