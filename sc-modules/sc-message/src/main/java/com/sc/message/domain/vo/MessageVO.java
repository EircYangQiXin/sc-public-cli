package com.sc.message.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内信消息视图对象
 */
@Data
@ApiModel(description = "站内信消息视图")
public class MessageVO {

    @ApiModelProperty(value = "消息ID", example = "1")
    private Long messageId;

    @ApiModelProperty(value = "消息标题", example = "系统公告")
    private String title;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息类型 0=通知 1=公告 2=私信", example = "0")
    private Integer msgType;

    @ApiModelProperty(value = "优先级 0=普通 1=重要 2=紧急", example = "0")
    private Integer priority;

    @ApiModelProperty(value = "发送者昵称", example = "管理员")
    private String senderName;

    @ApiModelProperty(value = "发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "是否已读 0=未读 1=已读", example = "0")
    private Integer isRead;

    @ApiModelProperty(value = "阅读时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
}
