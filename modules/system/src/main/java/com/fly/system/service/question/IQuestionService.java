package com.fly.system.service.question;

import com.fly.common.core.domain.TableDataInfo;
import com.fly.system.domain.question.dto.QuestionAddDTO;
import com.fly.system.domain.question.dto.QuestionQueryDTO;
import com.fly.system.domain.question.vo.QuestionVO;

import java.util.List;

public interface IQuestionService {
    List<QuestionVO> list(QuestionQueryDTO questionQueryDTO);
//
    boolean add(QuestionAddDTO questionAddDTO);
//
//    QuestionDetailVO detail(Long questionId);
//
//    int edit(QuestionEditDTO questionEditDTO);

    int delete(Long questionId);

}
