package com.fly.system.test.controller;

import com.fly.common.redis.service.RedisService;
import com.fly.system.domain.SysUser;
import com.fly.system.test.service.ITestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private ITestService testService;

    @Autowired
    private RedisService redisService;

    @GetMapping("/list")
    public List<?> list() {
        return testService.list();
    }

    @GetMapping("/log")
    public String log() {
        for(int i = 0; i < 10; i++) {
            log.info("我是info级别的日志");
        }
        log.error("我是error级别的日志");
        return "日志测试";
    }

    @GetMapping("/redisAddAndGet")
    public String redisAddAndGet() {
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount("redisTest");
        redisService.setCacheObject("u", sysUser);

        SysUser us = redisService.getCacheObject("u", SysUser.class);
        return us.toString();
    }
}
