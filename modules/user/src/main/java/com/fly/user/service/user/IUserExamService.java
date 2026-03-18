package com.fly.user.service.user;

import com.fly.common.core.domain.TableDataInfo;
import com.fly.user.domain.exam.dto.ExamQueryDTO;

public interface IUserExamService {

    int enter(String token, Long examId);

    TableDataInfo list(ExamQueryDTO examQueryDTO);
}

