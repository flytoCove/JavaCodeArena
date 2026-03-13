package com.fly.user.test;


import com.fly.common.core.controller.BaseController;
import com.fly.common.core.domain.R;
import com.fly.common.message.service.AliSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController extends BaseController {

    @Autowired
    private AliSmsService aliSmsService;

    @GetMapping("/sendCode")
    public R<Void> sendCode(String phone, String code) {
        log.info("验证码发送测试");
        return toR(aliSmsService.sendMobileCode(phone, code));
    }
}

