package com.fly.system.service.exam.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.common.core.constants.Constants;
import com.fly.common.core.enums.ResultCode;
import com.fly.common.security.exception.ServiceException;
import com.fly.system.domain.exam.Exam;
import com.fly.system.domain.exam.ExamQuestion;
import com.fly.system.domain.exam.dto.ExamAddDTO;
import com.fly.system.domain.exam.dto.ExamEditDTO;
import com.fly.system.domain.exam.dto.ExamQueryDTO;
import com.fly.system.domain.exam.dto.ExamQuestAddDTO;
import com.fly.system.domain.exam.vo.ExamDetailVO;
import com.fly.system.domain.exam.vo.ExamVO;
import com.fly.system.domain.question.Question;
import com.fly.system.domain.question.vo.QuestionVO;
//import com.fly.system.manager.ExamCacheManager;
import com.fly.system.mapper.exam.ExamMapper;
import com.fly.system.mapper.exam.ExamQuestionMapper;
import com.fly.system.mapper.question.QuestionMapper;
import com.fly.system.service.exam.IExamService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements IExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamQuestionMapper examQuestionMapper;
//
//    @Autowired
//    private ExamCacheManager examCacheManager;
//
    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public String add(ExamAddDTO examAddDTO) {
        checkExamSaveParams(examAddDTO, null);
        Exam exam = new Exam();
        BeanUtil.copyProperties(examAddDTO, exam);
        examMapper.insert(exam);
        return exam.getExamId().toString();
    }

//    @Override
//    public boolean questionAdd(ExamQuestAddDTO examQuestAddDTO) {
//        Exam exam = getExam(examQuestAddDTO.getExamId());
//        checkExam(exam);
//        if (Constants.TRUE.equals(exam.getStatus())) {
//            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
//        }
//        Set<Long> questionIdSet = examQuestAddDTO.getQuestionIdSet();
//        if (CollectionUtil.isEmpty(questionIdSet)) {
//            return true;
//        }
//        List<Question> questionList = questionMapper.selectBatchIds(questionIdSet);
//        if (CollectionUtil.isEmpty(questionList) || questionList.size() < questionIdSet.size()) {
//            throw new ServiceException(ResultCode.EXAM_QUESTION_NOT_EXISTS);
//        }
//        return saveExamQuestion(exam, questionIdSet);
//    }
    @Override
    public boolean questionAdd(ExamQuestAddDTO examQuestAddDTO) {
        Exam exam = getExam(examQuestAddDTO.getExamId());
        checkExam(exam);
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }

        Set<Long> questionIdSet = examQuestAddDTO.getQuestionIdSet();
        if (CollectionUtil.isEmpty(questionIdSet)) {
            return true;
        }

        // 1. 检查题目是否存在
        List<Question> questionList = questionMapper.selectBatchIds(questionIdSet);
        if (CollectionUtil.isEmpty(questionList) || questionList.size() < questionIdSet.size()) {
            throw new ServiceException(ResultCode.EXAM_QUESTION_NOT_EXISTS);
        }

        // 2. 查询该竞赛已有的题目
        List<ExamQuestion> existingQuestions = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamId, exam.getExamId())
                        .in(ExamQuestion::getQuestionId, questionIdSet)
        );

        // 3. 如果有重复的题目，移除它们
        if (CollectionUtil.isNotEmpty(existingQuestions)) {
            Set<Long> existingQuestionIds = existingQuestions.stream()
                    .map(ExamQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            questionIdSet.removeAll(existingQuestionIds);

            // 如果全部都是重复的，直接返回成功
            if (CollectionUtil.isEmpty(questionIdSet)) {
                return true;
            }
        }

        // 4. 只保存不重复的题目
        return saveExamQuestion(exam, questionIdSet);
    }


    @Override
    public int questionDelete(Long examId, Long questionId) {
        Exam exam = getExam(examId);
        checkExam(exam);
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }
        return examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId)
                .eq(ExamQuestion::getQuestionId, questionId));
    }

////    @Override
//    // 竞赛题目列表未维护题目在竞赛当中的顺序
////    public ExamDetailVO detailV1(Long examId) {
////        ExamDetailVO examDetailVO = new ExamDetailVO();
////        Exam exam = getExam(examId);
////        BeanUtil.copyProperties(exam, examDetailVO);
////        List<ExamQuestion> examQuestionList = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
////                .select(ExamQuestion::getQuestionId)
////                .eq(ExamQuestion::getExamId, examId)
////                .orderByAsc(ExamQuestion::getQuestionOrder));
////        if (CollectionUtil.isEmpty(examQuestionList)) {
////            //返回详情  只包含竞赛的基本信息
////            return examDetailVO;
////        }
////        List<Long> questionIdList = examQuestionList.stream().map(ExamQuestion::getQuestionId).toList();
////
////        //select  * from  tb_question question_id in (1,2,3)
////        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
////                .select(Question::getQuestionId, Question::getTitle, Question::getDifficulty)
////                .in(Question::getQuestionId, questionIdList));
//    ////        List<QuestionVO> questionVOList = new ArrayList<>();
////        List<QuestionVO> questionVOList = BeanUtil.copyToList(questionList, QuestionVO.class);
////        examDetailVO.setExamQuestionList(questionVOList);
////        return examDetailVO;
////    }

    @Override
    public ExamDetailVO detail(Long examId) {
        ExamDetailVO examDetailVO = new ExamDetailVO();
        Exam exam = getExam(examId);
        BeanUtil.copyProperties(exam, examDetailVO);
        List<QuestionVO> questionVOList = examQuestionMapper.selectExamQuestionList(examId);
        if (CollectionUtil.isEmpty(questionVOList)) {
            return examDetailVO;
        }
        examDetailVO.setExamQuestionList(questionVOList);
        return examDetailVO;
    }

    @Override
    public int edit(ExamEditDTO examEditDTO) {
        Exam exam = getExam(examEditDTO.getExamId());
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }
        checkExam(exam);
        checkExamSaveParams(examEditDTO, examEditDTO.getExamId());
        exam.setTitle(examEditDTO.getTitle());
        exam.setStartTime(examEditDTO.getStartTime());
        exam.setEndTime(examEditDTO.getEndTime());
        return examMapper.updateById(exam);
    }

    @Override
    public int delete(Long examId) {
        Exam exam = getExam(examId);
        if (Constants.TRUE.equals(exam.getStatus())) {
            throw new ServiceException(ResultCode.EXAM_IS_PUBLISH);
        }
        checkExam(exam);
        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId));
        return examMapper.deleteById(exam);
    }

    @Override
    public int publish(Long examId) {
        Exam exam = getExam(examId);
        //select count(0) from tb_exam_question where exam_id = #{examId}
        if (exam.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_IS_FINISH);
        }
        Long count = examQuestionMapper
                .selectCount(new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, examId));
        if (count == null || count <= 0) {
            throw new ServiceException(ResultCode.EXAM_NOT_HAS_QUESTION);
        }
        exam.setStatus(Constants.TRUE);

        //要将新发布的竞赛数据存储到redis   e:t:l  e:d:examId
        //examCacheManager.addCache(exam);
        return examMapper.updateById(exam);
    }

    @Override
    public int cancelPublish(Long examId) {
        Exam exam = getExam(examId);
        checkExam(exam);
        if (exam.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_IS_FINISH);
        }
        exam.setStatus(Constants.FALSE);
        //examCacheManager.deleteCache(examId);
        return examMapper.updateById(exam);
    }

    private void checkExamSaveParams(ExamAddDTO examSaveDTO, Long examId) {
        //1、竞赛标题是否重复进行判断   2、竞赛开始、结束时间进行判断
        List<Exam> examList = examMapper
                .selectList(new LambdaQueryWrapper<Exam>()
                        .eq(Exam::getTitle, examSaveDTO.getTitle())
                        .ne(examId != null, Exam::getExamId, examId));
        if (CollectionUtil.isNotEmpty(examList)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        if (examSaveDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_BEFORE_CURRENT_TIME);  //竞赛开始时间不能早于当前时间
        }
        if (examSaveDTO.getStartTime().isAfter(examSaveDTO.getEndTime())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_AFTER_END_TIME);
        }
    }

    private void checkExam(Exam exam) {
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_STARTED);
        }
    }

    private boolean saveExamQuestion(Exam exam, Set<Long> questionIdSet) {
        int num = 1;
        List<ExamQuestion> examQuestionList = new ArrayList<>();
        for (Long questionId : questionIdSet) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(exam.getExamId());
            examQuestion.setQuestionId(questionId);
            examQuestion.setQuestionOrder(num++);
            examQuestionList.add(examQuestion);
        }
        return saveBatch(examQuestionList);
    }

    private Exam getExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return exam;
    }
}

