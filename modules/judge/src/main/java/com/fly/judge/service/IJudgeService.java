package com.fly.judge.service;


import com.fly.api.domain.dto.JudgeSubmitDTO;
import com.fly.api.domain.vo.UserQuestionResultVO;

public interface IJudgeService {
    UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO);
}
