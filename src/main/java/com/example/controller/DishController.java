//开发时间 : 2022/10/29 17:15

package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.entity.DishFlavor;
import com.example.entity.dto.DishDto;
import com.example.entity.result.Result;
import com.example.service.CategoryService;
import com.example.service.DishFlavorService;
import com.example.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> save(/*HttpServletRequest request,*/@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        //已在过滤器中统一添加,这个可以省略
//        BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
        //自定义多表修改方法
        dishService.saveWithFlavor(dishDto);
        return Result.success("新增菜品成功");
    }

    /**
     * 难度 ☆☆☆☆☆
     * 分页查询数据
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> pageDtoInfo = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        lambdaQueryWrapper.like(name!=null,Dish::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,lambdaQueryWrapper);
        //对象拷贝,把pageInfo的内容拷贝到pageDtoInfo里面,但是不拷贝pageInfo中records属性
        BeanUtils.copyProperties(pageInfo,pageDtoInfo,"records");
        //单独处理pageInfo里面的Records
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item)->{
            DishDto dishdto = new DishDto();
            //将item的数据拷贝到dishDto中
            BeanUtils.copyProperties(item,dishdto);

            Long categoryId = item.getCategoryId(); //获取分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            //判断
            if (category != null){
                String categoryName = category.getName();
                dishdto.setCategoryName(categoryName);
            }
            return dishdto;
        }).collect(Collectors.toList());
        pageDtoInfo.setRecords(list);
        return Result.success(pageDtoInfo);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息,然后进行修改
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public Result<DishDto> get(@PathVariable Long id){
        DishDto byIdWithFlavor = dishService.getByIdWithFlavor(id);
        return Result.success(byIdWithFlavor);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> update(/*HttpServletRequest request,*/@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        //已在过滤器中统一添加,这个可以省略
//        BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
        //自定义多表修改方法
        dishService.updateWithFlavor(dishDto);
        return Result.success("新增菜品成功");
    }


    /**
     * 根据id停售/起售,批量停售/批量起售
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> status(/*HttpServletRequest request,*/ @PathVariable Integer status,Long[] ids){
        Dish dish = new Dish();
        //已在过滤器中统一添加,这个可以省略
//        BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
        for (Long id : ids) {
            dish.setStatus(status);
            dish.setId(id);
            dishService.updateById(dish);
        }
        return Result.success("菜品状态修改成功!");
    }

    /**
     * 根据id删除对应菜品信息和菜品口味信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(Long[] ids){
        dishService.deleteWithFlavor(ids);
        return Result.success("删除成功");
    }


//    /**
//     * 展示套餐菜品
//     *
//     * @return
//     */
//    @GetMapping("list")
//    public Result<List<Dish>> list(Dish dish){
//        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
//        lambdaQueryWrapper.eq(Dish::getStatus,1);
//        lambdaQueryWrapper.orderByAsc(Dish::getPrice).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(lambdaQueryWrapper);
////        log.info(list.toString());
//        return Result.success(list);
//    }

    /**
     * 客户端/后台展示菜品
     *
     * @return
     */
    @GetMapping("list")
    public Result<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        lambdaQueryWrapper.orderByAsc(Dish::getPrice).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(lambdaQueryWrapper);

        List<DishDto> dishDtoList = new ArrayList<>();
        for (Dish dish1 : list) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish1,dishDto);
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(DishFlavor::getDishId,dish1.getId());
            dishDto.setFlavors(dishFlavorService.list(lambdaQueryWrapper1));
            dishDtoList.add(dishDto);
        }


        return Result.success(dishDtoList);
    }
}
