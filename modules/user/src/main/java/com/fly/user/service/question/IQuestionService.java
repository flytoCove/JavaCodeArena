package com.fly.user.service.question;

import com.fly.common.core.domain.TableDataInfo;
import com.fly.user.domain.question.dto.QuestionQueryDTO;
import com.fly.user.domain.question.vo.QuestionDetailVO;
import com.fly.user.domain.question.vo.QuestionVO;

import java.util.List;

public interface IQuestionService {

    TableDataInfo list(QuestionQueryDTO questionQueryDTO);

//    List<QuestionVO> hotList();
//
    QuestionDetailVO detail(Long questionId);
//
//    String preQuestion(Long questionId);
//
//    String nextQuestion(Long questionId);
}

