package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
//        IdWorker idWorker = new IdWorker();
//        return weixinPayService.createNative(idWorker.nextId()+"","1");
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.searchPayLogFromRedis(name);
        if(payLog != null){
            return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
        }else {
            return new HashMap();
        }

    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result =null;
        int x = 0;
        while(true){
            Map map = weixinPayService.queryPayStatus(out_trade_no);
            x++;
            if(map == null){
                result = new Result(false,"支付出现错误");
                break;
            }

            if(map.get("trade_state").equals("SUCCESS")){
                result = new Result(true,"支付成功");
                orderService.updateOrderStatus(out_trade_no,map.get("transaction_id")+"");
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(x >= 100){
                result = new Result(false,"支付超时");
                break;
            }


        }

        return result;

    }
}
