package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.CookieUtil;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout=6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 购物车列表
     * @param
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //System.out.println(name);

        //读取cookie本地数据
        String cartListString  = CookieUtil.getCookieValue(request, "cartList", "utf-8");
        if(StringUtils.isEmpty(cartListString )) {
            cartListString  = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        
        if("anonymousUser".equals(name)){ //未登入
            return cartList_cookie;
        } else {    //已经登入
            //读取redis数据
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);

            if(cartList_cookie.size()>0){ //判断cookie中是否有购物车
                //合并购物车
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                //清空cookie数据
                CookieUtil.deleteCookie(request,response,"cartList");
                //将合并之后的购物车存入redis
                cartService.saveCartListToRedis(name, cartList_redis);
            }
            return cartList_redis;
        }

    }

    /**
     * 添加商品到购物车
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")   //允许跨域
    public Result addGoodsToCartList(Long itemId, Integer num){

        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");   //允许跨域的路径， * 表示该资源谁都可以用
       // response.setHeader("Access-Control-Allow-Credentials", "true");    //是否允许携带cookie

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //System.out.println(name);
        try {
            List<Cart> cartList = findCartList();
            List<Cart> carts = cartService.addGoodsToCartList(cartList, itemId, num);
            if("anonymousUser".equals(name)){ //未登入
                String cartsString = JSON.toJSONString(carts);
                CookieUtil.setCookie(request,response,"cartList",cartsString,3600,"utf-8");
            } else {    //已经登入
                //保存到redis中
                cartService.saveCartListToRedis(name,carts);
            }
            return new Result(true,"添加成功！");

        } catch (RuntimeException e) {

          return new Result(false,e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return new Result(false,"添加失败！");
        }

    }

}



