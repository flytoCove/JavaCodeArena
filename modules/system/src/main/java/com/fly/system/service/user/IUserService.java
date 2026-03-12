package com.fly.system.service.user;


import com.fly.system.domain.user.dto.UserDTO;
import com.fly.system.domain.user.dto.UserQueryDTO;
import com.fly.system.domain.user.vo.UserVO;

import java.util.List;

public interface IUserService {

    List<UserVO> list(UserQueryDTO userQueryDTO);

    int updateStatus(UserDTO userDTO);
}

