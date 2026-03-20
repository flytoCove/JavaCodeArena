package com.fly.judge.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fly.common.core.constants.Constants;
import com.fly.common.core.constants.JudgeConstants;
import com.fly.common.core.enums.CodeRunStatus;
import com.fly.judge.callback.DockerStartResultCallback;
import com.fly.judge.callback.StatisticsCallback;
import com.fly.judge.domain.CompileResult;
import com.fly.judge.domain.SandBoxExecuteResult;
import com.fly.judge.service.ISandboxService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 沙箱服务实现类
 * 负责基于Docker的Java代码隔离执行环境管理，包括代码编译、执行、资源限制和监控
 */
@Service
@Slf4j
public class SandboxServiceImpl implements ISandboxService {

    @Value("${sandbox.docker.host:unix:///Users/fly/.orbstack/run/docker.sock}")
    private String dockerHost;

    @Value("${sandbox.limit.memory:100000000}")
    private Long memoryLimit;

    @Value("${sandbox.limit.memory-swap:100000000}")
    private Long memorySwapLimit;

    @Value("${sandbox.limit.cpu:1}")
    private Long cpuLimit;

    @Value("${sandbox.limit.time:5}")
    private Long timeLimit;

    private DockerClient dockerClient;

    private String containerId;

    private String userCodeDir;

    private String userCodeFileName;


    /**
     * 执行Java代码的主流程方法
     *
     * @param userId 用户ID，用于创建唯一的代码文件目录
     * @param userCode 用户提交的Java代码
     * @param inputList 测试用例输入列表
     * @return SandBoxExecuteResult 沙箱执行结果，包含执行状态、输出列表、内存和时间消耗
     *
     * 执行流程：
     * 1. 创建用户代码文件
     * 2. 初始化Docker沙箱环境
     * 3. 编译用户代码
     * 4. 如果编译失败，清理资源并返回失败结果
     * 5. 如果编译成功，执行代码并返回结果
     */
    @Override
    public SandBoxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList) {
        createUserCodeFile(userId, userCode);
        initDockerSanBox();
        //编译代码
        CompileResult compileResult = compileCodeByDocker();
        if (!compileResult.isCompiled()) {
            deleteContainer();
            deleteUserCodeFile();
            return SandBoxExecuteResult.fail(CodeRunStatus.COMPILE_FAILED, compileResult.getExeMessage());
        }
        //执行代码
        return executeJavaCodeByDocker(inputList);
    }


    /**
     * 创建用户代码文件
     *
     * @param userId 用户ID，用于构建唯一目录
     * @param userCode 用户提交的Java代码内容
     *
     * 功能说明：
     * 1. 创建存放用户代码的根目录（如果不存在）
     * 2. 生成基于用户ID和时间戳的唯一目录名
     * 3. 创建完整的文件路径并将用户代码写入文件
     *
     * 文件命名格式：{userDir}/examCode/{userId}_{timestamp}/Solution.java
     */
    private void createUserCodeFile(Long userId, String userCode) {
        String examCodeDir = System.getProperty("user.dir") + File.separator + JudgeConstants.EXAM_CODE_DIR;
        if (!FileUtil.exist(examCodeDir)) {
            FileUtil.mkdir(examCodeDir); //创建存放用户代码的目录
        }
        String time = LocalDateTimeUtil.format(LocalDateTime.now(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        //拼接用户代码文件格式
        userCodeDir = examCodeDir + File.separator + userId + Constants.UNDERLINE_SEPARATOR + time;
        userCodeFileName = userCodeDir + File.separator + JudgeConstants.USER_CODE_JAVA_CLASS_NAME;
        FileUtil.writeString(userCode, userCodeFileName, Constants.UTF8);
    }


    /**
     * 初始化Docker沙箱环境
     *
     * 功能说明：
     * 1. 创建Docker客户端配置和连接
     * 2. 拉取Java运行环境镜像
     * 3. 创建带资源限制的容器
     * 4. 启动容器准备执行代码
     *
     * 容器配置包括：内存限制、CPU限制、目录挂载、网络禁用、只读根文件系统等安全限制
     */
    private void initDockerSanBox() {
        DefaultDockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        dockerClient = DockerClientBuilder
                .getInstance(clientConfig)
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
        //拉取镜像
        pullJavaEnvImage();
        //创建容器  限制资源   控制权限
        HostConfig hostConfig = getHostConfig();
        CreateContainerCmd containerCmd = dockerClient
                .createContainerCmd(JudgeConstants.JAVA_ENV_IMAGE)
                .withName(JudgeConstants.JAVA_CONTAINER_NAME);
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .withCmd("tail", "-f", "/dev/null")
                .exec();
        //记录容器id
        containerId = createContainerResponse.getId();
        //启动容器
        dockerClient.startContainerCmd(containerId).exec();
    }

    /**
     * 拉取Java执行环境Docker镜像
     *
     * 功能说明：
     * 1. 检查本地是否已存在所需镜像
     * 2. 如果不存在，则从远程仓库拉取
     * 3. 使用同步方式等待拉取完成
     *
     * 采用单次拉取策略，避免重复拉取
     */
    private void pullJavaEnvImage() {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        for (Image image : imageList) {
            String[] repoTags = image.getRepoTags();
            if (repoTags != null && repoTags.length > 0 && JudgeConstants.JAVA_ENV_IMAGE.equals(repoTags[0])) {
                return;
            }
        }
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(JudgeConstants.JAVA_ENV_IMAGE);
        try {
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 配置Docker容器的资源限制和安全策略
     *
     * @return HostConfig Docker主机配置对象
     *
     * 配置项包括：
     * 1. 目录挂载：将用户代码目录挂载到容器内
     * 2. 内存限制：限制容器最大内存使用
     * 3. CPU限制：限制容器CPU核心数
     * 4. 网络禁用：容器无法访问网络
     * 5. 根文件系统只读：防止容器内写操作
     */
    private HostConfig getHostConfig() {
        HostConfig hostConfig = new HostConfig();
        //设置挂载目录，指定用户代码路径
        hostConfig.setBinds(new Bind(userCodeDir, new Volume(JudgeConstants.DOCKER_USER_CODE_DIR)));
        //限制docker容器使用资源
        hostConfig.withMemory(memoryLimit);
        hostConfig.withMemorySwap(memorySwapLimit);
        hostConfig.withCpuCount(cpuLimit);
        hostConfig.withNetworkMode("none");  //禁用网络
        hostConfig.withReadonlyRootfs(true); //禁止在root目录写文件
        return hostConfig;
    }

    /**
     * 在Docker容器中编译Java代码
     *
     * @return CompileResult 编译结果，包含编译状态和错误信息
     *
     * 实现逻辑：
     * 1. 创建javac编译命令
     * 2. 异步执行编译并等待完成
     * 3. 通过回调结果判断编译是否成功
     * 4. 如果编译失败，捕获错误信息
     */
    private CompileResult compileCodeByDocker() {
        String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVAC_CMD, null, containerId);
        DockerStartResultCallback resultCallback = new DockerStartResultCallback();
        CompileResult compileResult = new CompileResult();
        try {
            dockerClient.execStartCmd(cmdId)
                    .exec(resultCallback)
                    .awaitCompletion();
            if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                compileResult.setCompiled(false);
                compileResult.setExeMessage(resultCallback.getErrorMessage());
            } else {
                compileResult.setCompiled(true);
            }
            return compileResult;
        } catch (InterruptedException e) {
            //此处可以直接抛出 已做统一异常处理  也可再做定制化处理
            throw new RuntimeException(e);
        }
    }

    /**
     * 在Docker容器中执行Java代码（支持多个测试用例）
     *
     * @param inputList 测试用例输入列表
     * @return SandBoxExecuteResult 执行结果，包含所有用例的输出、最大内存使用和最大执行时间
     *
     * 执行流程：
     * 1. 遍历每个测试用例输入
     * 2. 为每个用例创建执行命令
     * 3. 启动资源监控
     * 4. 执行代码并计时
     * 5. 记录输出、内存和时间
     * 6. 统计所有用例的最大值
     * 7. 清理容器和文件
     */
    private SandBoxExecuteResult executeJavaCodeByDocker(List<String> inputList) {
        List<String> outList = new ArrayList<>(); //记录输出结果
        long maxMemory = 0L;  //最大占用内存
        long maxUseTime = 0L; //最大运行时间
        //执行用户代码
        for (String inputArgs : inputList) {
            String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArgs, containerId);
            //执行代码
            StopWatch stopWatch = new StopWatch();        //执行代码后开始计时
            //执行情况监控
            StatsCmd statsCmd = dockerClient.statsCmd(containerId); //启动监控
            StatisticsCallback statisticsCallback = statsCmd.exec(new StatisticsCallback());
            stopWatch.start();
            DockerStartResultCallback resultCallback = new DockerStartResultCallback();
            try {
                dockerClient.execStartCmd(cmdId)
                        .exec(resultCallback)
                        .awaitCompletion(timeLimit, TimeUnit.SECONDS);
                if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                    //未通过所有用例返回结果
                    log.info("executeJavaCodeByDocker执行结果 {}",resultCallback.getMessage());
                    return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            finally {
//                deleteContainer();
//                deleteUserCodeFile();
//            }
            stopWatch.stop();  //结束时间统计
            statsCmd.close();  //结束docker容器执行统计
            long userTime = stopWatch.getLastTaskTimeMillis(); //执行耗时
            maxUseTime = Math.max(userTime, maxUseTime);       //记录最大的执行用例耗时
            Long memory = statisticsCallback.getMaxMemory();
            if (memory != null) {
                maxMemory = Math.max(maxMemory, statisticsCallback.getMaxMemory()); //记录最大的执行用例占用内存
            }
            outList.add(resultCallback.getMessage().trim());   //记录正确的输出结果
        }
        deleteContainer(); //删除容器
        deleteUserCodeFile(); //清理文件

        return getSanBoxResult(inputList, outList, maxMemory, maxUseTime); //封装结果
    }

    /**
     * 创建Docker容器内的执行命令
     *
     * @param javaCmdArr 基础命令数组
     * @param inputArgs 输入参数（多个参数用空格分隔）
     * @param containerId 容器ID
     * @return String 创建的命令ID
     *
     * 功能说明：
     * 1. 如果输入参数不为空，将其拼接到命令数组后
     * 2. 创建带有标准输入输出流的执行命令
     * 3. 返回命令ID供后续执行使用
     */
    private String createExecCmd(String[] javaCmdArr, String inputArgs, String containerId) {
        if (!StrUtil.isEmpty(inputArgs)) {
            //当入参不为空时拼接入参
            String[] inputArray = inputArgs.split(" "); //入参
            javaCmdArr = ArrayUtil.append(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArray);
        }
        ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(javaCmdArr)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        return cmdResponse.getId();
    }

    /**
     * 封装沙箱执行结果
     *
     * @param inputList 输入用例列表
     * @param outList 实际输出列表
     * @param maxMemory 最大内存使用
     * @param maxUseTime 最大执行时间
     * @return SandBoxExecuteResult 封装后的执行结果
     *
     * 逻辑说明：
     * 1. 验证输入和输出数量是否一致
     * 2. 不一致则返回执行异常结果
     * 3. 一致则返回成功结果
     */
    private SandBoxExecuteResult getSanBoxResult(List<String> inputList, List<String> outList,
                                                 long maxMemory, long maxUseTime) {
        if (inputList.size() != outList.size()) {
            //输入用例数量 不等于 输出用例数量  属于执行异常
            return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED, outList, maxMemory, maxUseTime);
        }
        return SandBoxExecuteResult.success(CodeRunStatus.SUCCEED, outList, maxMemory, maxUseTime);
    }


    /**
     * 删除Docker容器并释放连接
     *
     * 操作步骤：
     * 1. 停止容器运行
     * 2. 移除容器
     * 3. 关闭Docker客户端连接
     *
     * 用于资源清理，防止容器残留
     */
    private void deleteContainer() {
        //执行完成之后删除容器
        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
        //断开和docker连接
        try {
            dockerClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除用户代码文件及目录
     *
     * 用于清理临时生成的文件，防止磁盘空间占用
     * 删除整个用户代码目录及其所有内容
     */
    private void deleteUserCodeFile() {
        FileUtil.del(userCodeDir);
    }
}


