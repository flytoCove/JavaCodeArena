package com.fly.system.service.question.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fly.common.core.enums.ResultCode;
import com.fly.common.security.exception.ServiceException;
import com.fly.system.domain.question.Question;
import com.fly.system.domain.question.dto.QuestionAddDTO;
import com.fly.system.domain.question.dto.QuestionQueryDTO;
import com.fly.system.domain.question.vo.QuestionVO;
import com.fly.system.mapper.question.QuestionMapper;
import com.fly.system.service.question.IQuestionService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.pagehelper.PageHelper;
import java.util.List;

@Service
public class QuestionServiceImpl implements IQuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<QuestionVO> list(QuestionQueryDTO questionQueryDTO) {
        PageHelper.startPage(questionQueryDTO.getPageNum(), questionQueryDTO.getPageSize());
        return questionMapper.selectQuestionList(questionQueryDTO);
    }

    @Override
    public boolean add(QuestionAddDTO questionAddDTO) {

        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getTitle, questionAddDTO.getTitle()));
        if (CollectionUtil.isNotEmpty(questionList)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }

        Question question = new Question();
        BeanUtil.copyProperties(questionAddDTO, question);
        int insert = questionMapper.insert(question);
        return insert > 0;
    }


    @Override
    public int delete(Long questionId) {
        return 0;
    }
}
