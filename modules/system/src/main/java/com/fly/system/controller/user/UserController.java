package com.fly.system.controller.user;

import com.fly.common.core.controller.BaseController;
import com.fly.common.core.domain.R;
import com.fly.common.core.domain.TableDataInfo;
import com.fly.system.domain.user.dto.UserDTO;
import com.fly.system.domain.user.dto.UserQueryDTO;
import com.fly.system.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private IUserService userService;


    @GetMapping("/list")
    public TableDataInfo list(UserQueryDTO userQueryDTO) {
        return getTableDataInfo(userService.list(userQueryDTO));
    }

    @PutMapping("/updateStatus")
    //todo 拉黑：限制用户操作   解禁：放开对于用户限制
    //更新数据库中用户的状态信息。
    public R<Void> updateStatus(@RequestBody UserDTO userDTO) {
        return toR(userService.updateStatus(userDTO));
    }
}

