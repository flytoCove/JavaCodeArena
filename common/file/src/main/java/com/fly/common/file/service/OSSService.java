package com.fly.common.file.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.ObjectId;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.fly.common.core.constants.CacheConstants;
import com.fly.common.core.constants.Constants;
import com.fly.common.core.enums.ResultCode;
import com.fly.common.core.utils.ThreadLocalUtil;
import com.fly.common.file.config.OSSProperties;
import com.fly.common.file.config.OssClientFactory;
import com.fly.common.file.domain.OSSResult;
import com.fly.common.redis.service.RedisService;
import com.fly.common.security.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;


//@Service
//@RefreshScope
//public class OSSService {
//
//    @Autowired
//    private OSSProperties prop;
//
//    @Autowired
//    private OssClientFactory ossClientFactory;
//
//    @Autowired
//    private RedisService redisService;
//
//    @Value("${file.max-time}")
//    private int maxTime;
//
//    @Value("${file.test}")
//    private boolean test;
//
//
//    public OSSResult uploadFile(MultipartFile file) throws Exception {
//        if (!test) {
//            checkUploadCount();
//        }
//        InputStream inputStream = null;
//        try {
//            String fileName;
//            if (file.getOriginalFilename() != null) {
//                fileName = file.getOriginalFilename().toLowerCase();
//            } else {
//                fileName = "a.png";
//            }
//            String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
//            inputStream = file.getInputStream();
//            return upload(extName, inputStream);
//        } catch (Exception e) {
//            log.error("OSS upload file error", e);
//            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
//        } finally {
//            if (inputStream != null) {
//                inputStream.close();
//            }
//        }
//    }
//
//    private void checkUploadCount() {
//        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
//        Long times = redisService.getCacheMapValue(CacheConstants.USER_UPLOAD_TIMES_KEY, String.valueOf(userId), Long.class);
//        if (times != null && times >= maxTime) {
//            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD_TIME_LIMIT);
//        }
//        redisService.incrementHashValue(CacheConstants.USER_UPLOAD_TIMES_KEY, String.valueOf(userId), 1);
//        if (times == null || times == 0) {
//            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
//                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
//            redisService.expire(CacheConstants.USER_UPLOAD_TIMES_KEY, seconds, TimeUnit.SECONDS);
//        }
//    }

//    private OSSResult upload(String fileType, InputStream inputStream) {
//        // key pattern: file/id.xxx, cannot start with /
//        String key = prop.getPathPrefix() + ObjectId.next() + "." + fileType;
//        ObjectMetadata objectMetadata = new ObjectMetadata();
//        objectMetadata.setObjectAcl(CannedAccessControlList.PublicRead);
//        PutObjectRequest request = new PutObjectRequest(prop.getBucketName(), key, inputStream, objectMetadata);
//        PutObjectResult putObjectResult;
//        try {
//            putObjectResult = ossClient.putObject(request);
//        } catch (Exception e) {
//            log.error("OSS put object error: {}", ExceptionUtil.stacktraceToOneLineString(e, 500));
//            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
//        }
//        return assembleOSSResult(key, putObjectResult);
//    }
//
//
//    private OSSResult assembleOSSResult(String key, PutObjectResult putObjectResult) {
//        OSSResult ossResult = new OSSResult();
//        if (putObjectResult == null || StrUtil.isBlank(putObjectResult.getRequestId())) {
//            ossResult.setSuccess(false);
//        } else {
//            ossResult.setSuccess(true);
//            ossResult.setName(FileUtil.getName(key));
//        }
//        return ossResult;
//    }
//}
//

import java.util.UUID;
@Slf4j
@Service
public class OSSService {


    @Value("${file.max-time}")
    private int maxTime;

    @Value("${file.test}")
    private boolean test;

    private final OSSProperties ossProperties;
    private final OssClientFactory ossClientFactory;
    private final RedisService redisService;

    public OSSService(OSSProperties ossProperties, OssClientFactory ossClientFactory,RedisService redisService) {
        this.ossProperties = ossProperties;
        this.ossClientFactory = ossClientFactory;
        this.redisService = redisService;
    }



    public OSSResult upload(MultipartFile file) {
        if (!test) {
            checkUploadCount();
        }

        OSSResult ossResult = new OSSResult();

        try {
            // 1. 获取 OSS 客户端
            OSS ossClient = ossClientFactory.getClient();

            // 2. 获取文件输入流
            InputStream inputStream = file.getInputStream();

            // 3. 获取文件后缀名
            String originalFilename = file.getOriginalFilename();
            String suffix = ".png"; // 默认后缀
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String name = UUID.randomUUID().toString().replace("-", "") + suffix;
            // 4. 生成文件名
            String fileName = ossProperties.getPathPrefix() + name;

            // 5. 上传文件
            ossClient.putObject(
                    ossProperties.getBucketName(),
                    fileName,
                    inputStream
            );

            // 6. 设置 OSSResult
            ossResult.setSuccess(true);
            ossResult.setName(name); // 可以存文件名

        } catch (Exception e) {
            log.error("OSS upload file error", e);
            ossResult.setSuccess(false);
            ossResult.setName(null);
            // 上传失败也可以抛异常，根据业务决定
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
        }

        return ossResult;
    }

        private void checkUploadCount() {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        Long times = redisService.getCacheMapValue(CacheConstants.USER_UPLOAD_TIMES_KEY, String.valueOf(userId), Long.class);
        if (times != null && times >= maxTime) {
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD_TIME_LIMIT);
        }
        redisService.incrementHashValue(CacheConstants.USER_UPLOAD_TIMES_KEY, String.valueOf(userId), 1);
        if (times == null || times == 0) {
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            redisService.expire(CacheConstants.USER_UPLOAD_TIMES_KEY, seconds, TimeUnit.SECONDS);
        }
    }
}

