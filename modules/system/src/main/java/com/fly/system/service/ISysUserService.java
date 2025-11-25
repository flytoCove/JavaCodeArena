package com.fly.system.service;

import com.fly.common.core.domain.R;
import com.fly.system.controller.LoginResult;

public interface ISysUserService {
    R<Void> login(String userAccount, String password);
}
