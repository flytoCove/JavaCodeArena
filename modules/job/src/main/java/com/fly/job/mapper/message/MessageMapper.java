package com.fly.job.mapper.message;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.job.domain.message.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}

