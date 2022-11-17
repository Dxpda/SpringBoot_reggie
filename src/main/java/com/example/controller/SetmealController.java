//开发时间 : 2022/10/31 10:19

package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.BaseContext;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.entity.Setmeal;
import com.example.entity.SetmealDish;
import com.example.entity.dto.DishDto;
import com.example.entity.dto.SetmealDto;
import com.example.entity.result.Result;
import com.example.service.CategoryService;
import com.example.service.DishService;
import com.example.service.SetmealDishService;
import com.example.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    @GetMapping("/page")
    public Result<Page> page(Integer page,Integer pageSize,String name){
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> pageDtoInfo = new Page<>();
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name!=null,Setmeal::getName,name);
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,lambdaQueryWrapper);
        //把pageInfo的内容拷贝到pageDtoInfo,但是不拷贝records属性,因为要单独设置
        BeanUtils.copyProperties(pageInfo,pageDtoInfo,"records");

        List<Setmeal> records = pageInfo.getRecords();
        //因为Setmeal只有套餐的id,所以需要根据id把查询到的名字给到SetmealDto
//        List<SetmealDto> list = records.stream().map((a)->{
//            SetmealDto setmealDto = new SetmealDto();
//            BeanUtils.copyProperties(a,setmealDto);
//            Category byId = categoryService.getById(a.getCategoryId());
//            if (byId.getName() != null){
//                setmealDto.setCategoryName(byId.getName());
//            }
//            return setmealDto;
//        }).collect(Collectors.toList());

        List<SetmealDto> list = new ArrayList<>();
        for (Setmeal record : records) {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(record,setmealDto);
            Category byId = categoryService.getById(record.getCategoryId());
            if (byId != null){
                setmealDto.setCategoryName(byId.getName());
                list.add(setmealDto);
            }
        }
        pageDtoInfo.setRecords(list);
        return Result.success(pageDtoInfo);
    }

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public Result<String> save(/*HttpServletRequest request,*/@RequestBody SetmealDto setmealDto){
//        BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
        setmealService.saveSetmeal(setmealDto);
        return Result.success("添加成功");
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(Long[] ids){
        return setmealService.deleteSetmealWithSetmealDish(ids);
    }

    /**
     * 批量停售,停售,起售,批量起售
     * @return
     */
    @PostMapping("/status/{studes}")
    public Result<String> studes(/*HttpServletRequest request,*/@PathVariable Integer studes, Long[] ids){
        Setmeal setmeal = new Setmeal();
//        BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
        for (Long id : ids) {
            setmeal.setStatus(studes);
            setmeal.setId(id);
            setmealService.updateById(setmeal);
        }
        return Result.success("套餐状态已经更改成功!");
    }

    /**
     * 修改套餐前的数据展示
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealDto> getSetmeal(@PathVariable Long id){
        //调用在SetmealService自己写的方法
        SetmealDto setmealwithdish = setmealService.getSetmealwithdish(id);
        return Result.success(setmealwithdish);
    }

    /**
     * 修改套餐
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public Result<String> update(/*HttpServletRequest request,*/@RequestBody SetmealDto setmealDto){
        //已在过滤器中统一添加,这个可以省略
//        BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
        setmealService.updateSetmealwithSetmealDish(setmealDto);
        return Result.success("修改成功");
    }

    /**
     * 客户端展示套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public Result<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId()).eq(Setmeal::getStatus,setmeal.getStatus());
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(lambdaQueryWrapper);
        return Result.success(list);
    }

    /**
     * 客户端展示套餐里面的具体菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public Result<List<DishDto>> dish(@PathVariable Long id){
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.eq(SetmealDish::getSetmealId,id);
        //需要从这里面获取菜品id
        List<SetmealDish> list1 = setmealDishService.list(lambdaQueryWrapper1);
        List<DishDto> list2 = new ArrayList<>();
        for (SetmealDish setmealDish : list1) {
            DishDto dishDto = new DishDto();
            Dish byId = dishService.getById(setmealDish.getDishId());
            dishDto.setCopies(setmealDish.getCopies());
            BeanUtils.copyProperties(byId,dishDto);
            list2.add(dishDto);
        }
        return Result.success(list2);
    }
}



























