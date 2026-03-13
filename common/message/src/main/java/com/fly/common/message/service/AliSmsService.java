package com.fly.common.message.service;

import com.alibaba.fastjson2.JSON;
import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AliSmsService {

    @Autowired
    private Client aliClient;

    @Value("${sms.aliyun.templateCode:}")
    private String templateCode;

    @Value("${sms.aliyun.sign-name:}")
    private String signName;

    // 有效时长
    @Value("${sms.code-expiration:5}")
    private Long phoneCodeExpiration;

    /**
     * 发送验证码
     */
    public boolean sendMobileCode(String phone, String code) {

        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("min", phoneCodeExpiration.toString());

        return sendTempMessage(phone, signName, templateCode, params);
    }

    /**
     * 发送模板短信
     */
    public boolean sendTempMessage(String phone,
                                   String signName,
                                   String templateCode,
                                   Map<String, String> params) {

        try {

            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(JSON.toJSONString(params));

            SendSmsVerifyCodeResponse response =
                    aliClient.sendSmsVerifyCode(request);

            if (response.getBody() == null) {
                log.error("短信发送失败：返回body为空");
                return false;
            }

            if (!"OK".equalsIgnoreCase(response.getBody().getCode())) {
                log.error("短信发送失败: {}", response.getBody().getMessage());
                return false;
            }

            log.info("短信发送成功，手机号: {}", phone);
            return true;

        } catch (Exception e) {

            log.error("短信发送异常: {}", e.getMessage());
            return false;
        }
    }
}