//开发时间 : 2022/10/28 14:20

package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Setmeal;
import com.example.entity.dto.SetmealDto;
import com.example.entity.result.Result;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 添加套餐
     * @param setmealDto
     */
    public void saveSetmeal(SetmealDto setmealDto);
    /**
     * 删除套餐
     */
    public Result<String> deleteSetmealWithSetmealDish(Long[] ids);

    //根据id来获取修改前的数据回显
    public SetmealDto getSetmealwithdish(Long id);

    //修改setmeal和sermealDish的数据
    public void updateSetmealwithSetmealDish(SetmealDto setmealDto);
}
