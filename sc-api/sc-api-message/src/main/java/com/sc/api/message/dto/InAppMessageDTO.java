package com.sc.api.message.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 站内信发送 DTO（Feign 内部调用传输对象）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "站内信发送传输对象（内部调用）")
public class InAppMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "消息标题", required = true, example = "系统公告")
    private String title;

    @ApiModelProperty(value = "消息内容", required = true, example = "系统将于今晚22:00进行维护")
    private String content;

    @ApiModelProperty(value = "优先级 0=普通 1=重要 2=紧急", example = "0")
    private Integer priority;

    @ApiModelProperty(value = "接收用户ID列表", required = true)
    private List<Long> receiverIds;
}
