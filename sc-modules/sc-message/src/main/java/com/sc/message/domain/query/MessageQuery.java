package com.sc.message.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户侧消息查询参数
 */
@Data
@ApiModel(description = "站内信查询参数")
public class MessageQuery {

    @ApiModelProperty(value = "消息类型 0=通知 1=公告 2=私信")
    private Integer msgType;

    @ApiModelProperty(value = "是否已读 0=未读 1=已读")
    private Integer isRead;

    @ApiModelProperty(value = "关键词（标题模糊搜索）")
    private String keyword;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer pageSize = 10;
}
