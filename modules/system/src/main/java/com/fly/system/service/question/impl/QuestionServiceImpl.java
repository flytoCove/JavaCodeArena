package com.fly.system.service.question.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.fly.common.core.domain.TableDataInfo;
import com.fly.system.domain.question.dto.QuestionQueryDTO;
import com.fly.system.domain.question.vo.QuestionVO;
import com.fly.system.mapper.question.QuestionMapper;
import com.fly.system.service.question.IQuestionService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public int delete(Long questionId) {
        return 0;
    }
}
