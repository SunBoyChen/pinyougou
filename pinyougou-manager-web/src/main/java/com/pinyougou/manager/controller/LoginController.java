package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户登录是调用,显示名称
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("name")
    public Map name(){
        //获取spring security 用户名称
        String name= SecurityContextHolder.getContext().getAuthentication().getName();
        Map map=new HashMap();
        map.put("loginName", name);
        System.out.println(name);
        return map ;
    }
}
