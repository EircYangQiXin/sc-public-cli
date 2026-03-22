package com.sc.message.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理侧消息发送请求 DTO
 */
@Data
@ApiModel(description = "站内信发送请求")
public class MessageSendDTO {

    @NotBlank(message = "消息标题不能为空")
    @ApiModelProperty(value = "消息标题", required = true, example = "系统公告")
    private String title;

    @NotBlank(message = "消息内容不能为空")
    @ApiModelProperty(value = "消息内容", required = true)
    private String content;

    @ApiModelProperty(value = "消息类型 0=通知 1=公告 2=私信", example = "0")
    private Integer msgType;

    @ApiModelProperty(value = "优先级 0=普通 1=重要 2=紧急", example = "0")
    private Integer priority;

    @NotNull(message = "发送范围不能为空")
    @ApiModelProperty(value = "发送范围 USER=指定用户 ROLE=按角色 ALL=全员", required = true, example = "USER")
    private String sendScope;

    @ApiModelProperty(value = "接收用户ID列表（sendScope=USER时必填）")
    private List<Long> receiverIds;

    @ApiModelProperty(value = "角色ID列表（sendScope=ROLE时必填）")
    private List<Long> roleIds;
}
