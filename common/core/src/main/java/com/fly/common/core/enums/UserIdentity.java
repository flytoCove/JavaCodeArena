package com.fly.common.core.enums;

import lombok.Getter;

/**
 * 用来标识用户身份
 */
@Getter
public enum UserIdentity {

    ORDINARY(1, "普通用户"),

    ADMIN(2, "管理员");

    private Integer value;

    private String des;

    UserIdentity(Integer value, String des) {
        this.value = value;
        this.des = des;
    }
}

