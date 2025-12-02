package com.fly.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fly.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


// Entity：与数据库表中的字段一一对应的实体类
// DTO 接收前端的传递的数据（Data Transfer Object，数据传输对象），通常是轻量级的，只包含需要传输的数据
// VO：返回给前端的数据。
// VO（View Object，视图对象），用于在展示层显示数据，通常是将表示数据的实体对象中的
//一部分属性进行选择性的组合形成的一个新对象，目的是为了满足展示层数据要求的特定数据结构。
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
