package com.fly.system.service;

import com.fly.common.core.domain.R;
import com.fly.common.core.domain.vo.LoginUserVO;
import com.fly.system.domain.sysuser.dto.SysUserSaveDTO;

public interface ISysUserService {
    R<String> login(String userAccount, String password);

    boolean logout(String token);

    R<LoginUserVO> info(String token);

    int add(SysUserSaveDTO sysUserSaveDTO);
}
