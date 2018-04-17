package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.HttpClient;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //创建参数
        Map<String,String> param = new HashMap();
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "品优购");//商品描述
        param.put("out_trade_no", out_trade_no);//商户订单号
        param.put("total_fee",total_fee);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://test.itcast.cn");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型

        try {
            //生成xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求xml"+xmlParam);
            //发送请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            //获取结果
            String result = client.getContent();
            System.out.println("结果"+result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            System.out.println(resultMap);


            Map<String, String> map=new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", total_fee);//总金额
            map.put("out_trade_no",out_trade_no);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        //封装参数
        Map<String,String> param = new HashMap();
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url="https://api.mch.weixin.qq.com/pay/orderquery";

        //发送请求
        //生成xml
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求xml"+xmlParam);
            //发送请求
            HttpClient client = new HttpClient(url);
            //获取结果
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Map closePay(String out_trade_no) {

        //封装参数
        Map<String,String> param = new HashMap();
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url="https://api.mch.weixin.qq.com/pay/closeorder";

        //发送请求
        //生成xml
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求xml"+xmlParam);
            //发送请求
            HttpClient client = new HttpClient(url);
            //获取结果
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
