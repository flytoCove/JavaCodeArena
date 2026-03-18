package com.fly.job.mapper.exam;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.job.domain.exam.Exam;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

}
