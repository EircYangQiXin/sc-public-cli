package com.sc.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sc.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 站内信消息主表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message")
@ApiModel(description = "站内信消息")
public class SysMessage extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "消息ID", example = "1")
    private Long messageId;

    @ApiModelProperty(value = "消息标题", required = true, example = "系统公告")
    private String title;

    @ApiModelProperty(value = "消息内容", required = true)
    private String content;

    @ApiModelProperty(value = "消息类型 0=通知 1=公告 2=私信", example = "0")
    private Integer msgType;

    @ApiModelProperty(value = "发送范围 USER/ROLE/ALL", example = "USER")
    private String sendScope;

    @ApiModelProperty(value = "优先级 0=普通 1=重要 2=紧急", example = "0")
    private Integer priority;

    @ApiModelProperty(value = "发送者用户ID", example = "1")
    private Long senderId;

    @ApiModelProperty(value = "发送者昵称", example = "管理员")
    private String senderName;
}
