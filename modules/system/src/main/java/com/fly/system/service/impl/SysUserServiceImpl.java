package com.fly.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fly.common.core.domain.R;
import com.fly.common.core.domain.vo.LoginUserVO;
import com.fly.common.core.enums.ResultCode;
import com.fly.common.core.enums.UserIdentity;
import com.fly.common.security.exception.ServiceException;
import com.fly.common.security.service.TokenService;
import com.fly.system.domain.SysUser;
import com.fly.system.domain.sysuser.dto.SysUserSaveDTO;
import com.fly.system.mapper.SysUserMapper;
import com.fly.system.service.ISysUserService;
import com.fly.system.utils.BCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RefreshScope
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private TokenService tokenService;

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public R<String> login(String userAccount, String password) {
        // 1.通过账号去数据库查询对应的信息
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper.select(SysUser::getUserId,SysUser::getPassword)
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

        return R.ok(tokenService.createToken(sysUser.getUserId(), secret, UserIdentity.ADMIN.getValue(),"", null));
    }

    @Override
    public boolean logout(String token) {
        return false;
    }

    @Override
    public R<LoginUserVO> info(String token) {
        return null;
    }

    @Override
    public int add(SysUserSaveDTO sysUserSaveDTO) {
//        checkParams(sysUserSavaDTO);
        //重复
        //将 DTO 转为实体
        List<SysUser> sysUserList = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserAccount, sysUserSaveDTO.getUserAccount()));

        if (CollectionUtil.isNotEmpty(sysUserList)) {
            //用户已经存在
            //自定义的异常   公共的异常类
            throw new ServiceException(ResultCode.AILED_USER_EXISTS);
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount(sysUserSaveDTO.getUserAccount());
        sysUser.setPassword(BCryptUtils.encryptPassword(sysUserSaveDTO.getPassword()));
        // sysUser.setCreateBy(Constants.SYSTEM_USER_ID);
        return sysUserMapper.insert(sysUser);
    }
}
