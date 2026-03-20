package com.fly.user.service.user;


import com.fly.api.domain.vo.UserQuestionResultVO;
import com.fly.common.core.domain.R;
import com.fly.user.domain.user.dto.UserSubmitDTO;

public interface IUserQuestionService {
    R<UserQuestionResultVO> submit(UserSubmitDTO submitDTO);

    boolean rabbitSubmit(UserSubmitDTO submitDTO);

    UserQuestionResultVO exeResult(Long examId, Long questionId, String currentTime);
}
