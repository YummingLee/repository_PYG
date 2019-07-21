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
//1.根据商品 SKU ID 查询 SKU 商品信息
//2.获取商家 ID
//3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
//4.如果购物车列表中不存在该商家的购物车
    //4.1 新建购物车对象
    //4.2 将新建的购物车对象添加到购物车列表
//5.如果购物车列表中存在该商家的购物车
    // 查询购物车明细列表中是否存在该商品
    //5.1. 如果没有，新增购物车明细
    //5.2. 如果有，在原购物车明细上添加数量，更改金额

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoods2CartList(List<Cart> cartList, Long itemId, Integer num) {
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item == null){
            throw new RuntimeException("商品不存在");
        }

        if(!"1".equals(item.getStatus())){
            throw new RuntimeException("无效商品");
        }

        String sellerId = item.getSellerId();

        Cart cart = searchCartBySellerId(cartList, sellerId);

        if(cart == null){
            //创建购物车
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());

            List<TbOrderItem> orderItemList = new ArrayList<TbOrderItem>();
            TbOrderItem orderItem = createOrderItem(item,num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        }else {
           TbOrderItem orderItem = searchOrderByItemId(cart.getOrderItemList(),itemId);
           if(orderItem == null){
               orderItem = createOrderItem(item,num);
               cart.getOrderItemList().add(orderItem);
           }else {
               orderItem.setNum(orderItem.getNum()+num);
               orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
               //没有明细则移除
               if(orderItem.getNum() <= 0){
                   cart.getOrderItemList().remove(orderItem);
               }
               //没有商品移除该购物车
               if(cart.getOrderItemList().size() == 0){
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
        System.out.println("------ read from redis ------");
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList == null){
            cartList = new ArrayList<>();
        }

        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("-------save 2 redis -------");
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    //合并cookie redis购物车
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList1) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
               cartList2 = addGoods2CartList(cartList2,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList2;
    }

    private TbOrderItem searchOrderByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;

    }

    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if(num <= 0){
            throw new RuntimeException("非法数量");
        }

        TbOrderItem orderItem = new TbOrderItem();
//         Long itemId;
        orderItem.setItemId(item.getId());
//         Long goodsId;
        orderItem.setGoodsId(item.getGoodsId());
//         Long orderId;
//        orderItem.setOrderId();
//         String title;
        orderItem.setTitle(item.getTitle());
//         BigDecimal price;
        orderItem.setPrice(item.getPrice());
//         Integer num;
        orderItem.setNum(num);
//         BigDecimal totalFee;
        orderItem.setTotalFee(item.getPrice().multiply(new BigDecimal(num)));
//         String picPath;
        orderItem.setPicPath(item.getImage());
//         String sellerId;
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }

    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;

    }
}
