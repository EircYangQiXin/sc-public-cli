package com.sc.message.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 站内信接收人表实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_message_receiver")
@ApiModel(description = "站内信接收人")
public class SysMessageReceiver {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "消息ID")
    private Long messageId;

    @ApiModelProperty(value = "接收用户ID")
    private Long receiverId;

    @ApiModelProperty(value = "是否已读 0=未读 1=已读")
    private Integer isRead;

    @ApiModelProperty(value = "阅读时间")
    private LocalDateTime readTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
