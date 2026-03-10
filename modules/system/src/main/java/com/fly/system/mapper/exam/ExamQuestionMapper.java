package com.fly.system.mapper.exam;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.system.domain.exam.Exam;
import com.fly.system.domain.exam.ExamQuestion;
import com.fly.system.domain.exam.dto.ExamQueryDTO;
import com.fly.system.domain.exam.vo.ExamVO;
import com.fly.system.domain.question.dto.QuestionQueryDTO;
import com.fly.system.domain.question.vo.QuestionVO;

import java.util.List;

public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {

    List<QuestionVO> selectExamQuestionList(Long examId);
}

