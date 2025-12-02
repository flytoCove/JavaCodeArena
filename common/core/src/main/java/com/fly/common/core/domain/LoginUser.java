package com.fly.common.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUser {
    private Integer identity;  // 1.普通用户    2.管理员用户
}
