package com.fly.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fly.common.core.domain.R;
import com.fly.common.core.enums.ResultCode;
import com.fly.system.domain.SysUser;
import com.fly.system.mapper.SysUserMapper;
import com.fly.system.service.ISysUserService;
import com.fly.system.utils.BCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public R<Void> login(String userAccount, String password) {
        // 1.通过账号去数据库查询对应的信息
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper.select(SysUser::getPassword)
                .eq(SysUser::getUserAccount, userAccount));
        // 2.判断
        // R<Void> loginResult = new R<Void>();
        if (sysUser == null) {
//            loginResult.setCode(ResultCode.FAILED_USER_NOT_EXISTS.getCode());
//            loginResult.setMsg(ResultCode.FAILED_USER_NOT_EXISTS.getMsg());
//            return loginResult;

            return R.fail(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if (!BCryptUtils.matchesPassword(password, sysUser.getPassword())) {
//            loginResult.setCode(ResultCode.FAILED_LOGIN.getCode());
//            loginResult.setMsg(ResultCode.FAILED_LOGIN.getMsg());
//            return loginResult;

            return R.fail(ResultCode.FAILED_LOGIN);
        }

//        loginResult.setCode(ResultCode.SUCCESS.getCode());
//        loginResult.setMsg(ResultCode.SUCCESS.getMsg());
//        return loginResult;

        return R.ok();
    }
}
