package com.fly.user.mapper.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.user.domain.question.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

}

