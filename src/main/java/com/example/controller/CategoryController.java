//开发时间 : 2022/10/28 11:27

package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.BaseContext;
import com.example.entity.Category;
import com.example.entity.result.Result;
import com.example.service.CategoryService;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public Result<String> save(/*HttpServletRequest request,*/ @RequestBody Category category){
        log.info("category=>{}",category);
        //过滤器中已经添加了,这个可以省略
//        Long id = (Long) request.getSession().getAttribute("employee");
//        BaseContext.setCurrentId(id);//使用线程传id到自动填充类:MyMetaObjectHandler.java里面
        categoryService.save(category);
        return Result.success("添加分类成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize){
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //根据Sort设置排序
        lambdaQueryWrapper.orderByDesc(Category::getSort);
        //进行分页查询
        categoryService.page(pageInfo,lambdaQueryWrapper);
        return Result.success(pageInfo);
    }

    /**
     * 根据id删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(Long ids){
        log.info("删除分类,id=>{}",ids);

//        categoryService.removeById(ids);
        categoryService.remover(ids);
        return Result.success("分类信息删除成功");
    }

    /**
     * 修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public Result<String> update(/*HttpServletRequest request ,*/ @RequestBody Category category){
        log.info("修改分类:{}",category);
//        BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
        categoryService.updateById(category);

        return Result.success("修改分类信息成功");
    }

    /**
     * 根据分类条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        lambdaQueryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(lambdaQueryWrapper);

        return Result.success(list);
    }
}
