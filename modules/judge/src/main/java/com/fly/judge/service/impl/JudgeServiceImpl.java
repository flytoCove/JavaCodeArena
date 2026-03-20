package com.fly.judge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fly.api.domain.UserExeResult;
import com.fly.api.domain.dto.JudgeSubmitDTO;
import com.fly.api.domain.vo.UserQuestionResultVO;
import com.fly.common.core.constants.Constants;
import com.fly.common.core.constants.JudgeConstants;
import com.fly.common.core.enums.CodeRunStatus;
import com.fly.judge.domain.SandBoxExecuteResult;
import com.fly.judge.domain.UserSubmit;
import com.fly.judge.mapper.UserSubmitMapper;
import com.fly.judge.service.IJudgeService;
import com.fly.judge.service.ISandboxPoolService;
import com.fly.judge.service.ISandboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 判题服务实现类 V1
 * 负责Java代码的判题核心逻辑，包括代码执行、结果比对、性能评估和提交记录保存
 */
@Service
@Slf4j
public class JudgeServiceImpl implements IJudgeService {

    @Autowired
    private ISandboxService sandboxService;

    @Autowired
    private ISandboxPoolService sandboxPoolService;

    @Autowired
    private UserSubmitMapper userSubmitMapper;


    /**
     * 执行Java代码判题主流程
     *
     * @param judgeSubmitDTO 判题提交数据传输对象，包含用户ID、代码、输入输出列表、题目限制等信息
     * @return UserQuestionResultVO 判题结果视图对象，包含是否通过、得分、执行信息、详细执行结果等
     *
     * 处理流程：
     * 1. 调用沙箱服务执行用户代码
     * 2. 根据执行结果状态进行不同处理：
     *    - 执行成功：进入详细判题逻辑
     *    - 执行失败：记录失败信息并设置相应状态
     * 3. 保存用户提交记录
     * 4. 返回完整的判题结果
     */

    @Override
    public UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO) {
        log.info("---- 判题逻辑开始 -------");
        SandBoxExecuteResult sandBoxExecuteResult =
                sandboxPoolService.exeJavaCode(judgeSubmitDTO.getUserId(), judgeSubmitDTO.getUserCode(), judgeSubmitDTO.getInputList());
        UserQuestionResultVO userQuestionResultVO = new UserQuestionResultVO();
        log .info("-----doJudgeJavaCode----sandBoxExecuteResult:{}", JSON.toJSONString(sandBoxExecuteResult));
        if (sandBoxExecuteResult != null && CodeRunStatus.SUCCEED.equals(sandBoxExecuteResult.getRunStatus())) {
            //比对直接结果  时间限制  空间限制的比对
            userQuestionResultVO = doJudge(judgeSubmitDTO, sandBoxExecuteResult, userQuestionResultVO);
        } else {
            userQuestionResultVO.setPass(Constants.FALSE);
            if (sandBoxExecuteResult != null) {
                userQuestionResultVO.setExeMessage(sandBoxExecuteResult.getExeMessage());
            } else {
                userQuestionResultVO.setExeMessage(CodeRunStatus.UNKNOWN_FAILED.getMsg());
            }
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
        }
        saveUserSubmit(judgeSubmitDTO, userQuestionResultVO);
        log.info("判题逻辑结束，判题结果为： {} ", userQuestionResultVO);
        return userQuestionResultVO;
    }

    /**
     * 执行具体的判题逻辑
     *
     * @param judgeSubmitDTO 判题提交数据传输对象
     * @param sandBoxExecuteResult 沙箱执行结果
     * @param userQuestionResultVO 待填充的判题结果视图对象
     * @return UserQuestionResultVO 填充后的判题结果
     *
     * 处理步骤：
     * 1. 比对输出结果数量是否一致
     * 2. 逐条比对输入输出的对应关系
     * 3. 组装完整的判题结果
     */

    private UserQuestionResultVO doJudge(JudgeSubmitDTO judgeSubmitDTO,
                                         SandBoxExecuteResult sandBoxExecuteResult,
                                         UserQuestionResultVO userQuestionResultVO) {
        List<String> exeOutputList = sandBoxExecuteResult.getOutputList();
        List<String> outputList = judgeSubmitDTO.getOutputList();
        if (outputList.size() != exeOutputList.size()) {
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.NOT_ALL_PASSED.getMsg());
            return userQuestionResultVO;
        }
        List<UserExeResult> userExeResultList = new ArrayList<>();
        boolean passed = resultCompare(judgeSubmitDTO, exeOutputList, outputList, userExeResultList);
        return assembleUserQuestionResultVO(judgeSubmitDTO, sandBoxExecuteResult, userQuestionResultVO, userExeResultList, passed);
    }


    /**
     * 组装完整的判题结果视图对象
     *
     * @param judgeSubmitDTO 判题提交数据传输对象
     * @param sandBoxExecuteResult 沙箱执行结果
     * @param userQuestionResultVO 判题结果视图对象
     * @param userExeResultList 用户执行结果列表
     * @param passed 输出比对是否全部通过
     * @return UserQuestionResultVO 完整的判题结果
     *
     * 判断逻辑顺序：
     * 1. 首先判断输出结果是否正确
     * 2. 然后判断是否超出内存限制
     * 3. 最后判断是否超出时间限制
     * 4. 全部通过则计算得分
     */
    private UserQuestionResultVO assembleUserQuestionResultVO(JudgeSubmitDTO judgeSubmitDTO,
                                                              SandBoxExecuteResult sandBoxExecuteResult,
                                                              UserQuestionResultVO userQuestionResultVO,
                                                              List<UserExeResult> userExeResultList, boolean passed) {
        userQuestionResultVO.setUserExeResultList(userExeResultList);
        if (!passed) {
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.NOT_ALL_PASSED.getMsg());
            return userQuestionResultVO;
        }
        if (sandBoxExecuteResult.getUseMemory() > judgeSubmitDTO.getSpaceLimit()) {
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.OUT_OF_MEMORY.getMsg());
            return userQuestionResultVO;
        }
        if (sandBoxExecuteResult.getUseTime() > judgeSubmitDTO.getTimeLimit()) {
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.OUT_OF_TIME.getMsg());
            return userQuestionResultVO;
        }
        userQuestionResultVO.setPass(Constants.TRUE);
        int score = judgeSubmitDTO.getDifficulty() * JudgeConstants.DEFAULT_SCORE;
        userQuestionResultVO.setScore(score);
        return userQuestionResultVO;
    }


    /**
     * 比对预期输出和实际执行结果
     *
     * @param judgeSubmitDTO 判题提交数据传输对象，用于获取输入列表
     * @param exeOutputList 实际执行输出列表
     * @param outputList 预期输出列表
     * @param userExeResultList 用于存储每条输入的详细执行结果
     * @return boolean 所有输出是否完全匹配
     *
     * 功能：
     * 1. 逐条比对预期输出和实际输出
     * 2. 记录每条输入对应的预期输出和实际输出
     * 3. 当发现不匹配时，记录日志并标记失败
     */
    private boolean resultCompare(JudgeSubmitDTO judgeSubmitDTO, List<String> exeOutputList,
                                  List<String> outputList, List<UserExeResult> userExeResultList) {
        boolean passed = true;
        for (int index = 0; index < outputList.size(); index++) {
            String output = outputList.get(index);
            String exeOutPut = exeOutputList.get(index);
            String input = judgeSubmitDTO.getInputList().get(index);
            UserExeResult userExeResult = new UserExeResult();
            userExeResult.setInput(input);
            userExeResult.setOutput(output);
            userExeResult.setExeOutput(exeOutPut);
            userExeResultList.add(userExeResult);

            if (!output.equals(exeOutPut)) {
                passed = false;
                log.info("输入：{}， 期望输出：{}， 实际输出：{} ", input, output, exeOutPut);
            }
        }
        return passed;
    }

    /**
     * 保存用户提交记录
     *
     * @param judgeSubmitDTO 判题提交数据传输对象
     * @param userQuestionResultVO 判题结果视图对象
     *
     * 操作步骤：
     * 1. 将判题结果对象转换为用户提交实体
     * 2. 设置提交相关的各种属性
     * 3. 将详细执行结果列表转换为JSON字符串存储
     * 4. 删除该用户对该题目的历史提交记录（同一次考试）
     * 5. 插入新的提交记录
     *
     * 注意：条件删除时会根据examId是否为空动态构建查询条件
     */
    private void saveUserSubmit(JudgeSubmitDTO judgeSubmitDTO, UserQuestionResultVO userQuestionResultVO) {
        UserSubmit userSubmit = new UserSubmit();
        BeanUtil.copyProperties(userQuestionResultVO, userSubmit);
        userSubmit.setUserId(judgeSubmitDTO.getUserId());
        userSubmit.setQuestionId(judgeSubmitDTO.getQuestionId());
        userSubmit.setExamId(judgeSubmitDTO.getExamId());
        userSubmit.setProgramType(judgeSubmitDTO.getProgramType());
        userSubmit.setUserCode(judgeSubmitDTO.getUserCode());
        userSubmit.setCaseJudgeRes(JSON.toJSONString(userQuestionResultVO.getUserExeResultList()));
        userSubmit.setCreateBy(judgeSubmitDTO.getUserId());
        userSubmitMapper.delete(new LambdaQueryWrapper<UserSubmit>()
                .eq(UserSubmit::getUserId, judgeSubmitDTO.getUserId())
                .eq(UserSubmit::getQuestionId, judgeSubmitDTO.getQuestionId())
                .isNull(judgeSubmitDTO.getExamId() == null, UserSubmit::getExamId)
                .eq(judgeSubmitDTO.getExamId() != null, UserSubmit::getExamId, judgeSubmitDTO.getExamId()));
        userSubmitMapper.insert(userSubmit);
    }
}





