package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item == null){
            throw new RuntimeException("商品不存在");
        }

        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态无效");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        if(cart == null) {  //4.如果购物车列表中不存在该商家的购物车
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());

            //创建购物车明细，与订单详情相同
            TbOrderItem orderItem = createOrderItem(item,num);
            List<TbOrderItem> orderitemList = new ArrayList<>();
            orderitemList.add(orderItem);
            cart.setOrderItemList(orderitemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);

        } else {

            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem orderItem = searchOrderItemByItemId(orderItemList, itemId);
            if(orderItem == null){
                //5.1. 如果没有，新增购物车明细
                orderItem = new TbOrderItem();
                orderItem = createOrderItem(item,num);
                orderItemList.add(orderItem);
            } else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
                //如果数量小于0移除
                if(orderItem.getNum() <= 0) {
                    orderItemList.remove(orderItem);  //移除购物车明细
                }

                //如果移除后cart的明细数量为0，则将cart移除
                if(orderItemList.size() <= 0){
                    cartList.remove(cart);
                }

            }

        }

        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....."+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);

        if(cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        //将购物车1的商品添加到购物车2中
        for (Cart c : cartList1) {
           for (TbOrderItem orderItem:c.getOrderItemList()){
               addGoodsToCartList(cartList2,orderItem.getItemId(),orderItem.getNum());
           }

        }
        return cartList2;
    }

    /**
     * 根据商品明细ID查询
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId) {

        for (TbOrderItem t : orderItemList) {
            if(itemId.equals(t.getItemId())){
                return t;
            }
        }
        return null;
    }



    /**
     * 创建订单明细
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {

        if(num <= 0 ){
            new RuntimeException("数量异常");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setPrice(item.getPrice());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()* num));

        return  orderItem;
    }




    /**
     * 根据商家ID查询购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId) {

        for (Cart c : cartList) {
            if(sellerId.equals(c.getSellerId())){
                return c;
            }
        }
        return null;
    }
}
