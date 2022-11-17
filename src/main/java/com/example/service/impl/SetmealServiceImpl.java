//开发时间 : 2022/10/28 14:22

package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.SetmealDao;
import com.example.entity.Setmeal;
import com.example.entity.SetmealDish;
import com.example.entity.dto.SetmealDto;
import com.example.entity.result.Result;
import com.example.service.SetmealDishService;
import com.example.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealDao, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 添加套餐
     *
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveSetmeal(SetmealDto setmealDto) {
        this.save(setmealDto);
        Long id = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(id);
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐
     * @param ids
     */
    @Override
    public Result<String> deleteSetmealWithSetmealDish(Long[] ids) {
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        for (Long id : ids) {
            Setmeal byId = this.getById(id);
            if (byId.getStatus() == 1){
                return Result.error("\""+byId.getName()+"\""+"还未停售,请停售后再试!");
            }
            lambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
            setmealDishService.remove(lambdaQueryWrapper);
            this.removeById(id);
        }
        return Result.success("删除成功!");
    }

    /**
     * 根据id来获取修改前的数据回显
     *
     * @param id
     * @return
     */
    @Override
    public SetmealDto getSetmealwithdish(Long id) {
        Setmeal setmeal = new Setmeal();
        setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);


        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(lambdaQueryWrapper);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    /**
     * 修改套餐
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateSetmealwithSetmealDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lambdaQueryWrapper);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }
}
