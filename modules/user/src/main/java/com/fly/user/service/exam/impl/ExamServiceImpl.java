package com.fly.user.service.exam.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.fly.common.core.constants.Constants;
import com.fly.common.core.domain.TableDataInfo;
//import com.fly.common.core.utils.ThreadLocalUtil;
import com.fly.common.core.utils.ThreadLocalUtil;
import com.fly.user.domain.exam.dto.ExamQueryDTO;
import com.fly.user.domain.exam.dto.ExamRankDTO;
import com.fly.user.domain.exam.vo.ExamRankVO;
import com.fly.user.domain.exam.vo.ExamVO;
import com.fly.user.domain.user.vo.UserVO;
import com.fly.user.manager.ExamCacheManager;
import com.fly.user.manager.UserCacheManager;
import com.fly.user.mapper.exam.ExamMapper;
import com.fly.user.mapper.user.UserExamMapper;
import com.fly.user.service.exam.IExamService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ExamServiceImpl implements IExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamCacheManager examCacheManager;

    @Autowired
    private UserCacheManager userCacheManager;

    @Autowired
    private UserExamMapper userExamMapper;

    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public TableDataInfo redisList(ExamQueryDTO examQueryDTO) {
        //从redis当中获取  竞赛列表的数据
        Long total = examCacheManager.getListSize(examQueryDTO.getType(), null);
        List<ExamVO> examVOList;
        if (total == null || total <= 0) {
            examVOList = list(examQueryDTO);
            examCacheManager.refreshCache(examQueryDTO.getType(), null);
            total = new PageInfo<>(examVOList).getTotal();
        } else {
            examVOList = examCacheManager.getExamVOList(examQueryDTO, null);
            total = examCacheManager.getListSize(examQueryDTO.getType(), null);
        }
        if (CollectionUtil.isEmpty(examVOList)) {
            return TableDataInfo.empty();
        }
        assembleExamVOList(examVOList);
        return TableDataInfo.success(examVOList, total);
    }

//    @Override
//    public TableDataInfo rankList(ExamRankDTO examRankDTO) {
//        Long total = examCacheManager.getRankListSize(examRankDTO.getExamId());
//        List<ExamRankVO> examRankVOList;
//        if (total == null || total <= 0) {
//            PageHelper.startPage(examRankDTO.getPageNum(), examRankDTO.getPageSize());
//            examRankVOList = userExamMapper.selectExamRankList(examRankDTO.getExamId());
//            examCacheManager.refreshExamRankCache(examRankDTO.getExamId());
//            total = new PageInfo<>(examRankVOList).getTotal();
//        } else {
//            examRankVOList = examCacheManager.getExamRankList(examRankDTO);
//        }
//        if (CollectionUtil.isEmpty(examRankVOList)) {
//            return TableDataInfo.empty();
//        }
//        assembleExamRankVOList(examRankVOList);
//        return TableDataInfo.success(examRankVOList, total);
//    }
//
//    @Override
//    public String getFirstQuestion(Long examId) {
//        checkAndRefresh(examId);
//        return examCacheManager.getFirstQuestion(examId).toString();
//    }
//
//    @Override
//    public String preQuestion(Long examId, Long questionId) {
//        checkAndRefresh(examId);
//        return examCacheManager.preQuestion(examId, questionId).toString();
//    }
//
//    @Override
//    public String nextQuestion(Long examId, Long questionId) {
//        checkAndRefresh(examId);
//        return examCacheManager.nextQuestion(examId, questionId).toString();
//    }

    private void assembleExamVOList(List<ExamVO> examVOList) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        List<Long> userExamIdList = examCacheManager.getAllUserExamList(userId);
        if (CollectionUtil.isEmpty(userExamIdList)) {
            return;
        }
        for (ExamVO examVO : examVOList) {
            if (userExamIdList.contains(examVO.getExamId())) {
                examVO.setEnter(true);
            }
        }
    }

//    private void assembleExamRankVOList(List<ExamRankVO> examRankVOList) {
//        if (CollectionUtil.isEmpty(examRankVOList)) {
//            return;
//        }
//        for (ExamRankVO examRankVO : examRankVOList) {
//            Long userId = examRankVO.getUserId();
//            UserVO user = userCacheManager.getUserById(userId);
//            examRankVO.setNickName(user.getNickName());
//        }
//    }

//    private void checkAndRefresh(Long examId) {
//        Long listSize = examCacheManager.getExamQuestionListSize(examId);
//        if (listSize == null || listSize <= 0) {
//            examCacheManager.refreshExamQuestionCache(examId);
//        }
//    }
}

