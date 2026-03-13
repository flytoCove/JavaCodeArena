package com.fly.user.service.user;


import com.fly.common.core.domain.R;
import com.fly.common.core.domain.vo.LoginUserVO;
import com.fly.user.domain.user.dto.UserDTO;
import com.fly.user.domain.user.dto.UserUpdateDTO;
import com.fly.user.domain.user.vo.UserVO;

public interface IUserService {
    boolean sendCode(UserDTO userDTO);

    String codeLogin(String phone, String code);

//    boolean logout(String token);
//
//    R<LoginUserVO> info(String token);
//
//    UserVO detail();
//
//    int edit(UserUpdateDTO userUpdateDTO);
//
//    int updateHeadImage(String headImage);
}

