package com.fly.common.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class R<T> {

    private Integer code;

    private String msg;

    private T data;
}
