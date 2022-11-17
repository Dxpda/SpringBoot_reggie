//开发时间 : 2022/11/2 9:29

package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.common.BaseContext;
import com.example.entity.AddressBook;
import com.example.entity.result.Result;
import com.example.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查看地址
     * @param request
     * @return
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> address(HttpServletRequest request) {
        Long id = (Long) request.getSession().getAttribute("id");
        LambdaQueryWrapper<AddressBook> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AddressBook::getUserId,id);
        List<AddressBook> list = addressBookService.list(lambdaQueryWrapper);
        return Result.success(list);
    }

    /**
     * 添加地址
     * @param addressBook
     * @param request
     * @return
     */
    @PostMapping
    public Result<String> saveAddressBook(@RequestBody AddressBook addressBook,HttpServletRequest request){
        Long id = (Long) request.getSession().getAttribute("id");
        addressBook.setUserId(id);
        addressBookService.save(addressBook);
        return Result.success("添加成功");
    }

    /**
     * 编辑数据时回显数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<AddressBook> showAddressBook(@PathVariable Long id){
        AddressBook byId = addressBookService.getById(id);
        return Result.success(byId);
    }

    /**
     * 修改数据
     * @param addressBook
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody AddressBook addressBook){
        boolean b = addressBookService.updateById(addressBook);
        if (!b){
           return Result.error("修改失败");
        }
        return Result.success("修改成功!");
    }


    /**
     * 修改默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public Result<String> default_(@RequestBody AddressBook addressBook){
        LambdaUpdateWrapper<AddressBook> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        lambdaUpdateWrapper.set(AddressBook::getIsDefault,0);
        addressBookService.update(lambdaUpdateWrapper);
        addressBook.setIsDefault(1);
        boolean b = addressBookService.updateById(addressBook);
        return Result.success("设置默认地址成功!");
    }

    /**
     * 展示默认信息
     * @return
     */
    @GetMapping("/default")
    public Result<AddressBook> default_(){
        Long id = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AddressBook::getUserId,id);
        lambdaQueryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook one = addressBookService.getOne(lambdaQueryWrapper);
        return Result.success(one);
    }


}





















