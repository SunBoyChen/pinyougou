package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 品牌controller
 */
@Controller
@RequestMapping("/brand")
public class BrandController {

    //远程调用服务
    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    @ResponseBody
    public List<TbBrand> findAll(){
        System.out.println("findAll");
        return brandService.findAll();
    }

    /**
     * 分页查找
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/findPage")
    @ResponseBody
    public PageResult findPage(int page, int rows){
        PageResult pageResult = brandService.findPage(page, rows);
        return pageResult;
    }

    /**
     * 添加
     * @param
     * @return
     * 注意RequestBody
     */
    @RequestMapping("/add")
   @ResponseBody
    public Result add(@RequestBody TbBrand entity){
        try {
            System.out.println("add");
            brandService.add(entity);
            return new Result(true,"添加成功");
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("erro");
            return  new Result(false,"添加失败");
        }
    }

    /**
     * 修改
     * @param entity
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    public Result update(@RequestBody TbBrand entity){
        try {
            System.out.println("update");
            brandService.update(entity);
            return new Result(true,"修改成功");
        }catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"修改失败");
        }
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @RequestMapping("/findById")
    @ResponseBody
    public TbBrand findById(Long id){
        return brandService.findById(id);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Result delete(Long[] ids){
        try {
            System.out.println("delete");
            brandService.delete(ids);
            return new Result(true,"删除成功");
        }catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"删除失败");
        }
    }

    /**
     * 查询
     * @param searchEntity
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    @ResponseBody
    public PageResult search(@RequestBody TbBrand searchEntity,int page, int rows){
        System.out.println("search");
        return brandService.findPage(searchEntity,page,rows);
    }

    /**
     * 下拉列表数据
     * @return
     */
    @RequestMapping("/selectOptionList")
    @ResponseBody
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }




}












