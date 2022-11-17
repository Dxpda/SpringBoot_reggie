package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Dish;
import com.example.entity.dto.DishDto;

public interface DishService extends IService<Dish> {
    //新增菜品,同时插入菜品对应的口味数据,需要操作两张表
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息,同时还要更新对应的口味信息
    public void updateWithFlavor(DishDto dishDto);

    //删除菜品信息,同时删除对应的口味信息
    public void deleteWithFlavor(Long[] ids);
}
