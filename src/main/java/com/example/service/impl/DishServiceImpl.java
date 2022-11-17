//开发时间 : 2022/10/28 14:21

package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.DishDao;
import com.example.entity.Dish;
import com.example.entity.DishFlavor;
import com.example.entity.dto.DishDto;
import com.example.service.DishFlavorService;
import com.example.service.DishService;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional //因为是多表操作,所以要开启事务,同时成功,同时失败
public class DishServiceImpl extends ServiceImpl<DishDao, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品
        this.save(dishDto);
        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        //把菜品的id传入到菜品口味表里面去,否则无法表示当前口味是哪个菜的
//        for (DishFlavor flavor : flavors) {
//            flavor.setDishId(dishId);
//        }
        //使用流来为菜品口味添加菜品id
         flavors = flavors.stream().map((item)->{
           item.setDishId(dishId);//修改id为菜品的id
            return item;
        }).collect(Collectors.toList());//转换完成后又重新把当前数据转换为list集合

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息,从dish表查询
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        //将dish的数据拷贝到dishSto里面
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息,从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        List<DishFlavor> flavors = dishFlavorService.list(lambdaQueryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional //开启事务
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应的口味信息--dish_flavor表的delete操作,否则如果前端把数据清除,然后又重新加了两条,而后端没有清理,就会导致数据依然存在
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

//        添加当前提交过来的口味数据--dish_flavor表的insert操作
        List<DishFlavor> dishFlavors = dishDto.getFlavors();
        //因为getFlavors里面没有dish的id,所以需要手动赋值
        for (DishFlavor dishFlavor : dishFlavors) {
            dishFlavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(dishFlavors);
    }

    /**
     * 根据id删除两个表的数据
     * @param ids
     */
    @Override
    @Transactional
    public void deleteWithFlavor(Long[] ids) {
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        for (Long id : ids) {
            lambdaQueryWrapper.eq(DishFlavor::getDishId , id);
            dishFlavorService.remove(lambdaQueryWrapper);
            this.removeById(id);
        }
    }


}
