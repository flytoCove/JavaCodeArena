package com.fly.user.mapper.message;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.user.domain.message.MessageText;
import com.fly.user.domain.message.vo.MessageTextVO;

import java.util.List;

public interface MessageTextMapper extends BaseMapper<MessageText> {

    List<MessageTextVO> selectUserMsgList(Long userId);
}

