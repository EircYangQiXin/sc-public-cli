package com.sc.message.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读消息数量统计视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "未读消息数量统计")
public class MessageUnreadVO {

    @ApiModelProperty(value = "总未读数", example = "5")
    private Integer totalUnread;

    @ApiModelProperty(value = "通知未读数", example = "2")
    private Integer noticeUnread;

    @ApiModelProperty(value = "公告未读数", example = "1")
    private Integer bulletinUnread;

    @ApiModelProperty(value = "私信未读数", example = "2")
    private Integer letterUnread;
}
