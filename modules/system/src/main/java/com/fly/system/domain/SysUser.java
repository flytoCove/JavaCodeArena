package com.fly.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fly.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@TableName("tb_sys_user")
@Getter
@Setter
@ToString
public class SysUser extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)  // 使用雪花算法生成ID
    private Long userId;

    private String userAccount;

    private String password;

}
