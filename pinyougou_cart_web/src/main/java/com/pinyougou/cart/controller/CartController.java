package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference(timeout = 5000)
    private CartService cartService;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if(cartListStr == null || "".equals(cartListStr)){
            cartListStr = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListStr, Cart.class);
        if("anonymousUser".equals(name)){
            //未登录 从cookie里读取数据
            return cartList_cookie;
        }else {
            //已经登陆 从redis里读取数据
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
            if(cartList_cookie.size() > 0){
                cartList_redis = cartService.mergeCartList(cartList_redis,cartList_cookie); //合并购物车
                util.CookieUtil.deleteCookie(request,response,"cartList"); //删除cookie中的购物车
                cartService.saveCartListToRedis(name,cartList_redis); // 存入redis
            }

            return cartList_redis;
        }


    }

    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoods2CartList(cartList, itemId, num);
            if("anonymousUser".equals(name)) {
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 36000 * 24, "UTF-8");
            }else {
                cartService.saveCartListToRedis(name,cartList);
            }
            return new Result(true, "添加成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }


}
