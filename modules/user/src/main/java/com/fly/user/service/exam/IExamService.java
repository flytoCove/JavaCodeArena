package com.fly.user.service.exam;

import com.fly.common.core.domain.TableDataInfo;
import com.fly.user.domain.exam.dto.ExamQueryDTO;
import com.fly.user.domain.exam.dto.ExamRankDTO;
import com.fly.user.domain.exam.vo.ExamVO;

import java.util.List;

public interface IExamService {

    List<ExamVO> list(ExamQueryDTO examQueryDTO);

    TableDataInfo redisList(ExamQueryDTO examQueryDTO);
//
//    TableDataInfo rankList(ExamRankDTO examRankDTO);

    String getFirstQuestion(Long examId);

    String preQuestion(Long examId, Long questionId);

    String nextQuestion(Long examId, Long questionId);
}

