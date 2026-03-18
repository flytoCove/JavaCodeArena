package com.fly.user.mapper.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.user.domain.exam.Exam;
import com.fly.user.domain.exam.dto.ExamQueryDTO;
import com.fly.user.domain.exam.vo.ExamVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

    List<ExamVO> selectExamList(ExamQueryDTO examQueryDTO);

}
